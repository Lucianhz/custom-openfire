package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.group.ConcurrentGroupList;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.muc.ConflictException;
import org.jivesoftware.openfire.muc.ForbiddenException;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.openfire.muc.cluster.RoomUpdatedEvent;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.jivesoftware.openfire.plugin.rest.entity.GameProperty;
import org.jivesoftware.openfire.plugin.rest.entity.MUCChannelType;
import org.jivesoftware.openfire.plugin.rest.entity.MUCMemberEntity;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntity;
import org.jivesoftware.openfire.plugin.rest.entity.ParticipantEntities;
import org.jivesoftware.openfire.plugin.rest.entity.ParticipantEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.utils.DateUtils;
import org.jivesoftware.openfire.plugin.rest.utils.MUCRoomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.UserUtils;
import org.jivesoftware.util.AlreadyExistsException;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MUCRoomController.
 */
public class MUCRoomController {
    /**
     * The Constant INSTANCE.
     */
    public static final MUCRoomController INSTANCE = new MUCRoomController();
    private static final Logger Log = LoggerFactory.getLogger(MultiUserChatManager.class);

    /**
     * Gets the single instance of MUCRoomController.
     *
     * @return single instance of MUCRoomController
     */
    public static MUCRoomController getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the chat rooms.
     *
     * @param serviceName the service name
     * @param channelType the channel type
     * @param roomSearch  the room search
     * @return the chat rooms
     */
    public MUCRoomEntities getChatRooms(String serviceName, String channelType, String roomSearch, boolean expand) {
        List<MUCRoom> rooms = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRooms();

        List<MUCRoomEntity> mucRoomEntities = new ArrayList<MUCRoomEntity>();

        for (MUCRoom chatRoom : rooms) {
            if (roomSearch != null) {
                if (!chatRoom.getName().contains(roomSearch)) {
                    continue;
                }
            }

            if (channelType.equals(MUCChannelType.ALL)) {
                mucRoomEntities.add(convertToMUCRoomEntity(chatRoom, expand));
            } else if (channelType.equals(MUCChannelType.PUBLIC) && chatRoom.isPublicRoom()) {
                mucRoomEntities.add(convertToMUCRoomEntity(chatRoom, expand));
            }
        }

        return new MUCRoomEntities(mucRoomEntities);
    }

    /**
     * Gets the chat room.
     *
     * @param roomName    the room name
     * @param serviceName the service name
     * @return the chat room
     * @throws ServiceException the service exception
     */
    public MUCRoomEntity getChatRoom(String roomName, String serviceName, boolean expand) throws ServiceException {
        MUCRoom chatRoom = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName);

        if (chatRoom == null) {
            throw new ServiceException("Could not find the chat room", roomName, ExceptionType.ROOM_NOT_FOUND, Response.Status.NOT_FOUND);
        }

        MUCRoomEntity mucRoomEntity = convertToMUCRoomEntity(chatRoom, expand);
        return mucRoomEntity;
    }

    /**
     * Delete chat room.
     *
     * @param roomName    the room name
     * @param serviceName the service name
     * @throws ServiceException the service exception
     */
    public void deleteChatRoom(String roomName, String serviceName) throws ServiceException {
        MUCRoom chatRoom = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());

        if (chatRoom != null) {
            chatRoom.destroyRoom(null, null);
        } else {
            throw new ServiceException("Could not remove the channel", roomName, ExceptionType.ROOM_NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }

    /**
     * Creates the chat room.
     *
     * @param serviceName   the service name
     * @param mucRoomEntity the MUC room entity
     * @throws ServiceException the service exception
     */
    public void createChatRoom(String serviceName, MUCRoomEntity mucRoomEntity) throws ServiceException {
        try {
            createRoom(mucRoomEntity, serviceName);
        } catch (NotAllowedException e) {
            throw new ServiceException("Could not create the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not create the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not create the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        } catch (AlreadyExistsException e) {
            throw new ServiceException("Could not create the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.ALREADY_EXISTS, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Update chat room.
     *
     * @param roomName      the room name
     * @param serviceName   the service name
     * @param mucRoomEntity the MUC room entity
     * @throws ServiceException the service exception
     */
    public void updateChatRoom(String roomName, String serviceName, MUCRoomEntity mucRoomEntity)
            throws ServiceException {
        try {
            // If the room name is different throw exception
            if (!roomName.equals(mucRoomEntity.getRoomName())) {
                throw new ServiceException(
                        "Could not update the channel. The room name is different to the entity room name.", roomName,
                        ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }
            createRoom(mucRoomEntity, serviceName);
        } catch (NotAllowedException e) {
            throw new ServiceException("Could not update the channel", roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not update the channel", roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not update the channel", roomName, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        } catch (AlreadyExistsException e) {
            throw new ServiceException("Could not update the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.ALREADY_EXISTS, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Creates the room.
     *
     * @param mucRoomEntity the MUC room entity
     * @param serviceName   the service name
     * @throws NotAllowedException    the not allowed exception
     * @throws ForbiddenException     the forbidden exception
     * @throws ConflictException      the conflict exception
     * @throws AlreadyExistsException
     */
    private void createRoom(MUCRoomEntity mucRoomEntity, String serviceName) throws NotAllowedException,
            ForbiddenException, ConflictException, AlreadyExistsException {

        // Set owner
        JID owner = XMPPServer.getInstance().createJID("admin", null);
        if (mucRoomEntity.getOwners() != null && mucRoomEntity.getOwners().size() > 0) {
            owner = new JID(mucRoomEntity.getOwners().get(0));
        } else {
            List<String> owners = new ArrayList<String>();
            owners.add(owner.toBareJID());
            mucRoomEntity.setOwners(owners);
        }

        //	Check if chat service is available, if not create a new one
        boolean serviceRegistered = XMPPServer.getInstance().getMultiUserChatManager().isServiceRegistered(serviceName);
        if (!serviceRegistered) {
            XMPPServer.getInstance().getMultiUserChatManager().createMultiUserChatService(serviceName, serviceName, false);
        }

        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(mucRoomEntity.getRoomName().toLowerCase(), owner);

        // Set values
        room.setNaturalLanguageName(mucRoomEntity.getNaturalName());
        room.setSubject(mucRoomEntity.getSubject());
        room.setDescription(mucRoomEntity.getDescription());
        room.setPassword(mucRoomEntity.getPassword());
        room.setPersistent(mucRoomEntity.isPersistent());
        room.setPublicRoom(mucRoomEntity.isPublicRoom());
        room.setRegistrationEnabled(mucRoomEntity.isRegistrationEnabled());
        room.setCanAnyoneDiscoverJID(mucRoomEntity.isCanAnyoneDiscoverJID());
        room.setCanOccupantsChangeSubject(mucRoomEntity.isCanOccupantsChangeSubject());
        room.setCanOccupantsInvite(mucRoomEntity.isCanOccupantsInvite());
        room.setChangeNickname(mucRoomEntity.isCanChangeNickname());
        room.setModificationDate(mucRoomEntity.getModificationDate());
        room.setLogEnabled(mucRoomEntity.isLogEnabled());
        room.setLoginRestrictedToNickname(mucRoomEntity.isLoginRestrictedToNickname());
        room.setMaxUsers(mucRoomEntity.getMaxUsers());
        room.setMembersOnly(mucRoomEntity.isMembersOnly());
        room.setModerated(mucRoomEntity.isModerated());

        // Fire RoomUpdateEvent if cluster is started
        if (ClusterManager.isClusteringStarted()) {
            CacheFactory.doClusterTask(new RoomUpdatedEvent((LocalMUCRoom) room));
        }

        // Set broadcast presence roles
        if (mucRoomEntity.getBroadcastPresenceRoles() != null) {
            room.setRolesToBroadcastPresence(mucRoomEntity.getBroadcastPresenceRoles());
        } else {
            room.setRolesToBroadcastPresence(new ArrayList<String>());
        }
        // Set all roles
        setRoles(room, mucRoomEntity);

        // Set creation date
        if (mucRoomEntity.getCreationDate() != null) {
            room.setCreationDate(mucRoomEntity.getCreationDate());
        } else {
            room.setCreationDate(new Date());
        }

        // Set modification date
        if (mucRoomEntity.getModificationDate() != null) {
            room.setModificationDate(mucRoomEntity.getModificationDate());
        } else {
            room.setModificationDate(new Date());
        }

        // Unlock the room, because the default configuration lock the room.
        room.unlock(room.getRole());

        // Save the room to the DB if the room should be persistant
        if (room.isPersistent()) {
            room.saveToDB();
        }
    }

    /**
     * Gets the room participants.
     *
     * @param roomName    the room name
     * @param serviceName the service name
     * @return the room participants
     */
    public ParticipantEntities getRoomParticipants(String roomName, String serviceName) {
        ParticipantEntities participantEntities = new ParticipantEntities();
        List<ParticipantEntity> participants = new ArrayList<ParticipantEntity>();

        Collection<MUCRole> serverParticipants = XMPPServer.getInstance().getMultiUserChatManager()
                .getMultiUserChatService(serviceName).getChatRoom(roomName).getParticipants();

        for (MUCRole role : serverParticipants) {
            ParticipantEntity participantEntity = new ParticipantEntity();
            participantEntity.setJid(role.getRoleAddress().toFullJID());
            participantEntity.setRole(role.getRole().name());
            participantEntity.setAffiliation(role.getAffiliation().name());

            participants.add(participantEntity);
        }

        participantEntities.setParticipants(participants);
        return participantEntities;
    }

    /**
     * Convert to MUC room entity.
     *
     * @param room the room
     * @return the MUC room entity
     */
    public MUCRoomEntity convertToMUCRoomEntity(MUCRoom room, boolean expand) {
        MUCRoomEntity mucRoomEntity = new MUCRoomEntity(room.getNaturalLanguageName(), room.getName(),
                room.getDescription());

        mucRoomEntity.setSubject(room.getSubject());
        mucRoomEntity.setCanAnyoneDiscoverJID(room.canAnyoneDiscoverJID());
        mucRoomEntity.setCanChangeNickname(room.canChangeNickname());
        mucRoomEntity.setCanOccupantsChangeSubject(room.canOccupantsChangeSubject());
        mucRoomEntity.setCanOccupantsInvite(room.canOccupantsInvite());

        mucRoomEntity.setPublicRoom(room.isPublicRoom());
        mucRoomEntity.setPassword(room.getPassword());
        mucRoomEntity.setPersistent(room.isPersistent());
        mucRoomEntity.setRegistrationEnabled(room.isRegistrationEnabled());
        mucRoomEntity.setLogEnabled(room.isLogEnabled());
        mucRoomEntity.setLoginRestrictedToNickname(room.isLoginRestrictedToNickname());
        mucRoomEntity.setMaxUsers(room.getMaxUsers());
        mucRoomEntity.setMembersOnly(room.isMembersOnly());
        mucRoomEntity.setModerated(room.isModerated());

        ConcurrentGroupList<JID> owners = new ConcurrentGroupList<JID>(room.getOwners());
        ConcurrentGroupList<JID> admins = new ConcurrentGroupList<JID>(room.getAdmins());
        ConcurrentGroupList<JID> members = new ConcurrentGroupList<JID>(room.getMembers());
        ConcurrentGroupList<JID> outcasts = new ConcurrentGroupList<JID>(room.getOutcasts());

        if (expand) {
            for (Group ownerGroup : owners.getGroups()) {
                owners.addAllAbsent(ownerGroup.getAll());
            }
            for (Group adminGroup : admins.getGroups()) {
                admins.addAllAbsent(adminGroup.getAll());
            }
            for (Group memberGroup : members.getGroups()) {
                members.addAllAbsent(memberGroup.getAll());
            }
            for (Group outcastGroup : outcasts.getGroups()) {
                outcasts.addAllAbsent(outcastGroup.getAll());
            }
        }

        mucRoomEntity.setOwners(MUCRoomUtils.convertJIDsToStringList(owners));
        mucRoomEntity.setAdmins(MUCRoomUtils.convertJIDsToStringList(admins));
        mucRoomEntity.setMembers(MUCRoomUtils.convertJIDsToStringList(members));
        mucRoomEntity.setOutcasts(MUCRoomUtils.convertJIDsToStringList(outcasts));

        mucRoomEntity.setOwnerGroups(MUCRoomUtils.convertGroupsToStringList(owners.getGroups()));
        mucRoomEntity.setAdminGroups(MUCRoomUtils.convertGroupsToStringList(admins.getGroups()));
        mucRoomEntity.setMemberGroups(MUCRoomUtils.convertGroupsToStringList(members.getGroups()));
        mucRoomEntity.setOutcastGroups(MUCRoomUtils.convertGroupsToStringList(outcasts.getGroups()));

        mucRoomEntity.setBroadcastPresenceRoles(room.getRolesToBroadcastPresence());

        mucRoomEntity.setCreationDate(room.getCreationDate());
        mucRoomEntity.setModificationDate(room.getModificationDate());

        return mucRoomEntity;
    }

    /**
     * Reset roles.
     *
     * @param room          the room
     * @param mucRoomEntity the muc room entity
     * @throws ForbiddenException  the forbidden exception
     * @throws NotAllowedException the not allowed exception
     * @throws ConflictException   the conflict exception
     */
    private void setRoles(MUCRoom room, MUCRoomEntity mucRoomEntity) throws ForbiddenException, NotAllowedException,
            ConflictException {
        List<JID> roles = new ArrayList<JID>();
        Collection<JID> owners = new ArrayList<JID>();
        Collection<JID> existingOwners = new ArrayList<JID>();

        List<JID> mucRoomEntityOwners = MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners());
        owners.addAll(room.getOwners());

        // Find same owners
        for (JID jid : owners) {
            if (mucRoomEntityOwners.contains(jid)) {
                existingOwners.add(jid);
            }
        }

        // Don't delete the same owners
        owners.removeAll(existingOwners);
        room.addOwners(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners()), room.getRole());

        // Collect all roles to reset
        roles.addAll(owners);
        roles.addAll(room.getAdmins());
        roles.addAll(room.getMembers());
        roles.addAll(room.getOutcasts());

        for (JID jid : roles) {
            room.addNone(jid, room.getRole());
        }

        room.addOwners(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners()), room.getRole());
        if (mucRoomEntity.getAdmins() != null) {
            room.addAdmins(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getAdmins()), room.getRole());
        }
        if (mucRoomEntity.getMembers() != null) {
            for (String memberJid : mucRoomEntity.getMembers()) {
                room.addMember(new JID(memberJid), null, room.getRole());
            }
        }
        if (mucRoomEntity.getOutcasts() != null) {
            for (String outcastJid : mucRoomEntity.getOutcasts()) {
                room.addOutcast(new JID(outcastJid), null, room.getRole());
            }
        }
    }

    /**
     * Adds the admin.
     *
     * @param serviceName the service name
     * @param roomName    the room name
     * @param jid         the jid
     * @throws ServiceException the service exception
     */
    public void addAdmin(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addAdmin(UserUtils.checkAndGetJID(jid), room.getRole());
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not add admin", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not add admin", jid, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Adds the owner.
     *
     * @param serviceName the service name
     * @param roomName    the room name
     * @param jid         the jid
     * @throws ServiceException the service exception
     */
    public void addOwner(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addOwner(UserUtils.checkAndGetJID(jid), room.getRole());
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not add owner", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        }
    }

    /**
     * Adds the member.
     *
     * @param serviceName the service name
     * @param roomName    the room name
     * @param jid         the jid
     * @throws ServiceException the service exception
     */
    public void addMember(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addMember(UserUtils.checkAndGetJID(jid), null, room.getRole());
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not add member", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not add member", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        }
    }

    public void addMembers(String serviceName, String roomName, List<String> list) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        String jid = null;
        try {
            for (String j : list) {
                jid = j;
                room.addMember(UserUtils.checkAndGetJID(j), null, room.getRole());
            }
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not add member", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not add member", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        }
    }

    /**
     * Adds the outcast.
     *
     * @param serviceName the service name
     * @param roomName    the room name
     * @param jid         the jid
     * @throws ServiceException the service exception
     */
    public void addOutcast(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addOutcast(UserUtils.checkAndGetJID(jid), null, room.getRole());
        } catch (NotAllowedException e) {
            throw new ServiceException("Could not add outcast", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not add outcast", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not add outcast", jid, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Delete affiliation.
     *
     * @param serviceName the service name
     * @param roomName    the room name
     * @param jid         the jid
     * @throws ServiceException the service exception
     */
    public void deleteAffiliation(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addNone(UserUtils.checkAndGetJID(jid), room.getRole());
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not delete affiliation", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not delete affiliation", jid, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        }
    }

    public boolean updateRoomPic(String roomName, String roomPic) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_ROOM);
            pstmt.setString(1, roomPic);
            pstmt.setString(2, "00" + new Date().getTime());
            pstmt.setString(3, roomName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
        return false;
    }

    public List<Map<String, Object>> findRoomUsers(String roomName) {
        try {
            try (Connection con = DbConnectionManager.getConnection()) {
                try (PreparedStatement statement = con.prepareStatement(SELECT_USERS)) {
                    statement.setString(1, roomName);
                    statement.setString(2, roomName);
                    try (ResultSet rs = statement.executeQuery()) {
                        List<Map<String, Object>> list = new ArrayList<>();
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("roomID", rs.getInt("roomID"));
                            map.put("jid", rs.getString("jid"));
                            map.put("remark", rs.getString("remark"));
                            map.put("affiliation", rs.getString("affiliation"));
                            map.put("offlineDate", rs.getString("offlineDate"));
                            map.put("owner", rs.getBoolean("owner"));
                            list.add(map);
                        }
                        return list;
                    }
                }
            }
        } catch (SQLException ex) {
            Log.error("findRoomUsers", ex);
            return Collections.emptyList();
        }
    }

    public boolean remarkChanged(String name, String jid, String remark) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_MEMBER);
            pstmt.setString(1, remark);
            pstmt.setString(2, jid);
            pstmt.setString(3, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
        return false;
    }

    public String findRoomPic(String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ROOMPIC);
            pstmt.setString(1, roomName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                return rs.getString("roomPic");
            }
            return null;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

    }

    public MUCRoomEntities findMUCRooms(String jid) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MUCRoomEntity room = null;
        MUCRoomEntities rooms = new MUCRoomEntities();
        List<MUCRoomEntity> list = new ArrayList<MUCRoomEntity>();
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ROOMS.toString());
            pstmt.setString(1, jid);
            pstmt.setString(2, jid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                room = new MUCRoomEntity();
                room.setRoomId(rs.getLong("roomID"));
                room.setRoomName(rs.getString("name"));
                room.setNaturalName(rs.getString("naturalName"));
                room.setCreationDate(DateUtils.timeFormat(rs.getString("creationDate")));
                room.setModificationDate(DateUtils.timeFormat(rs.getString("modificationDate")));
                room.setDescription(rs.getString("description"));
                room.setMaxUsers(rs.getInt("maxUsers"));
                room.setRoomPic(rs.getString("roomPic"));
                room.setSubject(rs.getString("subject"));
                room.setRoomMark(rs.getLong("roomMark"));
                room.setPassword(rs.getString("roomPassword"));
                room.setType(rs.getString("type"));
                room.setRedPackAmount(rs.getString("redPackAmount"));
                room.setRedPackTotle(rs.getString("redPackTotle"));
                room.setMultiple(rs.getBigDecimal("multiple"));
                list.add(room);
            }
            rooms.setMucRooms(list);
            return rooms;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return null;
    }

    private String SELECT_GAME_ROOM = "SELECT * FROM ofmucroom WHERE type != '0' LIMIT ?,? ";

    public MUCRoomEntities findGameRooms(Integer start, Integer pageSize) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MUCRoomEntity room = null;
        MUCRoomEntities rooms = new MUCRoomEntities();
        List<MUCRoomEntity> list = new ArrayList<MUCRoomEntity>();
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_GAME_ROOM);
            pstmt.setInt(1, start);
            pstmt.setInt(2, pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                room = new MUCRoomEntity();
                room.setRoomId(rs.getLong("roomID"));
                room.setRoomName(rs.getString("name"));
                room.setNaturalName(rs.getString("naturalName"));
                room.setCreationDate(DateUtils.timeFormat(rs.getString("creationDate")));
                room.setModificationDate(DateUtils.timeFormat(rs.getString("modificationDate")));
                room.setDescription(rs.getString("description"));
                room.setMaxUsers(rs.getInt("maxUsers"));
                room.setRoomPic(rs.getString("roomPic"));
                room.setSubject(rs.getString("subject"));
                room.setRoomMark(rs.getLong("roomMark"));
                room.setPassword(rs.getString("roomPassword"));
                room.setType(rs.getString("type"));
                room.setRedPackAmount(rs.getString("redPackAmount"));
                room.setRedPackTotle(rs.getString("redPackTotle"));
                room.setMultiple(rs.getBigDecimal("multiple"));
                list.add(room);
            }
            rooms.setMucRooms(list);
            return rooms;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return null;
    }

    public Long findMUCRoomMark(String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ROOMMARK);
            pstmt.setString(1, roomName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                return rs.getLong("roomMark");
            }
            return null;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    public MUCRoomEntity findRoomDetail(String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MUCRoomEntity room = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ROOMDETAIL);
            pstmt.setString(1, roomName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                room = new MUCRoomEntity();
                room.setRoomId(rs.getLong("roomID"));
                room.setCreationDate(DateUtils.timeFormat(rs.getString("creationDate")));
                room.setDescription(rs.getString("description"));
                room.setMaxUsers(rs.getInt("maxUsers"));
                room.setNaturalName(rs.getString("naturalName"));
                room.setRoomMark(rs.getLong("roomMark"));
                room.setRoomName(rs.getString("name"));
                room.setRoomPic(rs.getString("roomPic"));
                room.setSubject(rs.getString("subject"));
                room.setPassword(rs.getString("roomPassword"));
                room.setType(rs.getString("type"));
                room.setMultiple(rs.getBigDecimal("multiple"));
                room.setRedPackTotle(rs.getInt("redPackTotle") + "");
                room.setRedPackAmount(rs.getString("redPackAmount"));
                room.setPardonBack(rs.getBigDecimal("pardonBack"));
                room.setIsValidate(rs.getInt("isValidate"));
            }
            return room;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return null;
    }

    public List<Map<String, String>> findRoomRemarks(String jid) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_REMARKS.toString());
            pstmt.setString(1, jid);
            pstmt.setString(2, jid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map = new HashMap<String, String>();
                map.put("roomName", rs.getString("name"));
                map.put("remark", rs.getString("remark"));
                map.put("offlineDate", rs.getString("offlineDate"));
                map.put("affiliation", rs.getString("affiliation"));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return null;
    }

    public String findRoomRemark(String jid, String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_REMARK.toString());
            pstmt.setString(1, roomName);
            pstmt.setString(2, jid);
            pstmt.setString(3, roomName);
            pstmt.setString(4, jid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                return rs.getString("remark");
            }
            return null;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

    }

    public MUCMemberEntity findMember(String jid, String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MUCMemberEntity mm = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_MEMBER);
            pstmt.setString(1, jid);
            pstmt.setString(2, roomName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                mm = new MUCMemberEntity();
                mm.setRoomID(rs.getInt("roomID"));
                mm.setJid(rs.getString("jid"));
                mm.setRemark("remark");
            }
            return mm;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return null;
    }

    public boolean updateAffiliationRemark(String jid, String roomName, String remark) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_AFFILIATION_REMARK);
            pstmt.setString(1, remark);
            pstmt.setString(2, jid);
            pstmt.setString(3, roomName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
        return false;
    }

    public int findAffiliation(String roomName, String jid) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int i = 0;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_AFFILIATION_BYJID);
            pstmt.setString(1, roomName);
            pstmt.setString(2, jid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                i = rs.getInt(1);
            }
            return i;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return i;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    public int findRoomUserTotle(Long roomNo) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int i = 0;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ROOMUSERTOTLE_BYROOMMARK.toString());
            pstmt.setLong(1, roomNo);
            pstmt.setLong(2, roomNo);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                i = rs.getInt(1);
            }
            return i;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return i;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    public boolean updateMucmemberOfflineDateByJid(String jid, String time) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_MEMBER_OFFLINEDATE);
            pstmt.setString(1, time);
            pstmt.setString(2, jid);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public boolean updateAffiliatiomOfflineDateByJid(String jid, String time) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_AFFILIATION_OFFLINEDATE);
            pstmt.setString(1, time);
            pstmt.setString(2, jid);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public boolean updateRoomTypeByName(String roomName, String type) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_ROOM_TYPE);
            pstmt.setString(1, type);
            pstmt.setString(2, roomName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public boolean updateRoomByName(Map<String, Object> map) {
        Connection con = null;
        Statement state = null;
        StringBuffer sql = new StringBuffer("UPDATE ofmucroom SET ");
        if (map == null) {
            return false;
        }
        if (map.get("name") == null && "".equals(map.get("name"))) {
            return false;
        }
        if (map.get("type") != null && !"".equals(map.get("type"))) {
            sql.append("type = '" + map.get("type") + "' ,");
        }
        if (map.get("naturalName") != null && !"".equals(map.get("naturalName"))) {
            sql.append("naturalName = '" + map.get("naturalName") + "' ,");
        }
        if (map.get("description") != null && !"".equals(map.get("description"))) {
            sql.append("description = '" + map.get("description") + "' ,");
        }
        if (map.get("maxUsers") != null && !"".equals(map.get("maxUsers"))) {
            sql.append("maxUsers= " + map.get("maxUsers") + " ,");
        }
        if (map.get("roomPassword") != null && !"".equals(map.get("roomPassword"))) {
            sql.append("roomPassword= '" + map.get("roomPassword") + "' ,");
        }
        if (map.get("redPackTotle") != null && !"".equals(map.get("redPackTotle"))) {
            sql.append("redPackTotle= " + map.get("redPackTotle") + " ,");
        }
        if (map.get("redPackAmount") != null && !"".equals(map.get("redPackAmount"))) {
            sql.append("redPackAmount= '" + map.get("redPackAmount") + "' ,");
        }
        if (map.get("multiple") != null && !"".equals(map.get("multiple"))) {
            sql.append("multiple= " + map.get("multiple") + " ,");
        }
        if (map.get("pardon") != null && !"".equals(map.get("pardon"))) {
            sql.append("pardon= '" + map.get("pardon") + "' ,");
        }
        if (map.get("commission") != null && !"".equals(map.get("commission"))) {
            sql.append("commission= " + map.get("commission") + " ,");
        }
        if (map.get("modificationDate") != null && !"".equals(map.get("modificationDate"))) {
            sql.append("modificationDate= '" + map.get("modificationDate") + "' ,");
        }
        if (map.get("parentReferralCode") != null && !"".equals(map.get("parentReferralCode"))) {
            sql.append("parentReferralCode= '" + map.get("parentReferralCode") + "' ,");
        }
        if (map.get("pardonBack") != null && !"".equals(map.get("pardonBack"))) {
            sql.append("pardonBack= " + map.get("pardonBack") + " ,");
        }
        if (map.get("selfManagement") != null && !"".equals(map.get("selfManagement"))) {
            sql.append("selfManagement= " + map.get("selfManagement") + " ,");
        }
        if (map.get("state") != null) {
            sql.append("state= " + map.get("state") + " ,");
        }
        if (map.get("img") != null && !"".equals(map.get("img"))) {
            sql.append("img= '" + map.get("img") + "' ,");
        }
        String str = sql.toString().substring(0, sql.toString().length() - 1) + "WHERE name = '" + map.get("name") + "' ";
        try {
            Log.info(str);
            con = DbConnectionManager.getConnection();
            state = con.createStatement();
            return state.executeUpdate(str) > 0;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(state, con);
        }
    }

    public String findRoomPardonByRoomName(String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ROOM_PARDON);
            pstmt.setString(1, roomName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                return rs.getString("pardon");
            }
            return null;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    private String SELECT_ROOM_PARDON = "SELECT pardon FROM ofmucroom WHERE name = ?";
    private String UPDATE_ROOM_TYPE = "UPDATE ofmucroom SET type = ? WHERE name = ? ";
    private String UPDATE_MEMBER_OFFLINEDATE = "UPDATE ofmucmember SET offlineDate = ? WHERE jid = ?";
    private String UPDATE_AFFILIATION_OFFLINEDATE = "UPDATE ofmucaffiliation SET offlineDate = ? WHERE jid = ?";
    private static final StringBuffer SELECT_ROOMUSERTOTLE_BYROOMMARK = new StringBuffer();

    static {
        SELECT_ROOMUSERTOTLE_BYROOMMARK.append("SELECT count(*) FROM ( ");
        SELECT_ROOMUSERTOTLE_BYROOMMARK.append(" SELECT roomID FROM ofmucmember WHERE roomID = ");
        SELECT_ROOMUSERTOTLE_BYROOMMARK.append(" (SELECT roomID FROM ofmucroom WHERE roomMark = ?) ");
        SELECT_ROOMUSERTOTLE_BYROOMMARK.append(" UNION ALL ");
        SELECT_ROOMUSERTOTLE_BYROOMMARK.append(" SELECT roomID FROM ofmucaffiliation WHERE roomID = ");
        SELECT_ROOMUSERTOTLE_BYROOMMARK.append(" (SELECT roomID FROM ofmucroom WHERE roomMark = ?)) m");
    }

    private String SELECT_AFFILIATION_BYJID = "SELECT affiliation FROM ofmucaffiliation WHERE roomID = (SELECT roomID FROM ofmucroom WHERE name = ? ) AND jid = ?";
    private String UPDATE_AFFILIATION_REMARK = "UPDATE ofmucaffiliation SET remark = ? WHERE jid = ? AND roomID = (SELECT roomID FROM ofmucroom WHERE name = ? )";
    private String SELECT_MEMBER = "SELECT * FROM ofmucmember WHERE jid = ? AND roomID = (SELECT roomID FROM ofmucroom WHERE name = ?)";
    private static final StringBuffer SELECT_REMARKS = new StringBuffer();

    static {
        SELECT_REMARKS.append("SELECT r.name ,a.remark,a.offlineDate,affiliation FROM ofmucroom r JOIN  ");
        SELECT_REMARKS.append(" (SELECT remark,roomID ,offlineDate,url affiliation FROM ofmucmember WHERE jid = ? ");
        SELECT_REMARKS.append(" UNION ");
        SELECT_REMARKS.append(" SELECT remark,roomID ,offlineDate,affiliation FROM ofmucaffiliation WHERE jid = ? ) a ");
        SELECT_REMARKS.append(" ON a.roomID = r.roomID ");
    }

    private static final StringBuffer SELECT_REMARK = new StringBuffer();

    static {
        SELECT_REMARK.append("SELECT remark FROM ofmucaffiliation WHERE roomID = ( ");
        SELECT_REMARK.append(" SELECT roomID FROM ofmucroom WHERE name = ? ) AND jid = ? ");
        SELECT_REMARK.append(" UNION ");
        SELECT_REMARK.append(" SELECT remark FROM ofmucmember WHERE roomID = ( ");
        SELECT_REMARK.append(" SELECT roomID FROM ofmucroom WHERE name = ? ) AND jid = ? ");
    }

    private String SELECT_ROOMDETAIL = "SELECT r.roomID,r.creationDate,r.description,r.maxUsers,r.naturalName,r.roomMark,r.name,r.roomPic,r.subject,r.roomPassword,r.type,r.redPackTotle,r.redPackAmount,r.multiple,r.pardonBack,r.isValidate FROM ofmucroom r WHERE r.name = ?";
    private String SELECT_ROOMMARK = "SELECT r.roomMark FROM ofmucroom r  WHERE r.name = ? ";
    private static final StringBuffer SELECT_ROOMS = new StringBuffer();

    static {
        SELECT_ROOMS.append("SELECT	* FROM ofmucroom r WHERE r.roomID in ");
        SELECT_ROOMS.append(" (SELECT m.roomID FROM ofmucmember m  WHERE m.jid = ? ");
        SELECT_ROOMS.append(" UNION ");
        SELECT_ROOMS.append(" SELECT a.roomID  FROM ofmucaffiliation a WHERE  a.jid = ?)");
    }

    private String SELECT_ROOMPIC = "SELECT r.roomPic FROM ofmucroom r WHERE name = ?";
    private String UPDATE_MEMBER = "UPDATE ofmucmember SET remark = ?  WHERE  jid = ? and roomID = ( SELECT r.roomID FROM ofmucroom r WHERE name= ? )";

    private static final String SELECT_USERS;

    static {
        SELECT_USERS = " SELECT false as owner,m.roomID,m.jid,m.remark ,m.url affiliation,datediff(now(),FROM_UNIXTIME(m.offlineDate/1000)) offlineDate FROM ofmucmember m WHERE m.roomID = (SELECT r.roomID FROM ofmucroom r WHERE name = ? )" +
                " UNION " +
                " SELECT true as owner,a.roomID,a.jid ,a.remark ,a.affiliation,datediff(now(),FROM_UNIXTIME(a.offlineDate/1000)) offlineDate FROM ofmucaffiliation a  WHERE a.roomID = (SELECT r.roomID FROM ofmucroom r WHERE name = ?)" +
                " order by if(isnull(affiliation),1,0),affiliation";
    }

    private String UPDATE_ROOM = "UPDATE ofmucroom SET roomPic = ? , modificationDate = ? WHERE name = ?";


    private String SELECT_GAMEPROPERTY_BYROOMID = "SELECT * FROM game_properties WHERE room_id = ?";
    private String UPDATE_GAMEPROPERTY_BYROOMIDKEY = "UPDATE game_properties SET value = ? WHERE room_id = ? and `key` = ?";
    private String SELECT_GAMEPROPERTY_VALUE = "SELECT value FROM game_properties WHERE room_id = ? and `key` = ?";
    private String INSERT_GAMEPROPERTY = "INSERT INTO  game_properties(`room_id`,`key`,`value`) VALUES(?,?,?)";
    private String SELECT_ROOM_BYID = "SELECT name FROM ofmucroom WHERE roomID = ? ";
    private String UPDATE_ROOMPROPERTY_BYID = "UPDATE ofmucroom SET redPackAmount = ? WHERE roomID = ?";
    private String DELETE_ROOMVALIDATE = "DELETE FROM ofmucroomvalidate WHERE id = ?";
    private String UPDATE_MUCROOM_ISVALIDATE = "UPDATE ofmucroom SET isValidate = ? , modificationDate = ? WHERE name = ? ";
    private String INSERT_ROOMVALIDATE = "INSERT INTO ofmucroomvalidate(username,roomName,beInvited,createDate,modifyDate) VALUES(?,?,?,?,?)";
    private String SELECT_ROOMVALIDATES = "SELECT a.id,a.username,a.beInvited FROM ofmucroomvalidate a WHERE roomName = ? ORDER BY createDate DESC LIMIT ? ,?";
    private String SELECT_ROOMVALIDATE_BYUSERNAMEANDROOMNAMEANDBEINVITED = "SELECT id FROM ofmucroomvalidate WHERE username = ? AND roomName = ? AND beInvited = ?";

    public boolean isExsitsRoomValidate(String username, String roomName, String beInvited) {
        Connection con = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(SELECT_ROOMVALIDATE_BYUSERNAMEANDROOMNAMEANDBEINVITED);
            pre.setString(1, username);
            pre.setString(2, roomName);
            pre.setString(3, beInvited);
            rs = pre.executeQuery();
            while (rs.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            Log.error("room isExsitsRoomValidate exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(rs, pre, con);
        }
    }

    public List<Map<String, Object>> findRoomValidateList(String roomName, Integer start, Integer size) {
        Connection con = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(SELECT_ROOMVALIDATES);
            pre.setString(1, roomName);
            pre.setInt(2, start);
            pre.setInt(3, size);
            rs = pre.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", rs.getInt("id"));
                map.put("username", rs.getString("username"));
                map.put("beInvited", rs.getString("beInvited"));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            Log.error("room findRoomValidateList exception: {}", e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pre, con);
        }
    }

    public boolean updateRoomIsValidate(Integer isValidate, String roomName) {
        Connection con = null;
        PreparedStatement pre = null;
        String date = new Date().getTime() + "";
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(UPDATE_MUCROOM_ISVALIDATE);
            pre.setInt(1, isValidate);
            pre.setString(2, date);
            pre.setString(3, roomName);
            return pre.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error("room updateRoomIsValidate exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public boolean insertRoomValidate(String username, String roomName, String beInvited) {
        Connection con = null;
        PreparedStatement pre = null;
        String date = new Date().getTime() + "";
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(INSERT_ROOMVALIDATE);
            pre.setString(1, username);
            pre.setString(2, roomName);
            pre.setString(3, beInvited);
            pre.setString(4, date);
            pre.setString(5, date);
            return pre.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error("room insertRoomValidate exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public boolean deleteRoomValidateById(Integer id) {
        Connection con = null;
        PreparedStatement pre = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(DELETE_ROOMVALIDATE);
            pre.setInt(1, id);
            return pre.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error("room RoomValidateByRoomNameAndUsername exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public boolean updateRoomPropertyById(Long roomId, String redPackAmount) {
        Connection con = null;
        PreparedStatement pre = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(UPDATE_ROOMPROPERTY_BYID);
            pre.setString(1, redPackAmount);
            pre.setLong(2, roomId);
            return pre.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error("room updateRoomPropertyById exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public boolean isExistsRoomById(Long roomId) {
        Connection con = null;
        PreparedStatement pre = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(SELECT_ROOM_BYID);
            pre.setLong(1, roomId);
            return pre.executeQuery().next();
        } catch (SQLException e) {
            Log.error("room isExistsRoomById exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public boolean insertGameProperties(Long roomId, String key, String value) {
        Connection con = null;
        PreparedStatement pre = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(INSERT_GAMEPROPERTY);
            pre.setLong(1, roomId);
            pre.setString(2, key);
            pre.setString(3, value);
            return pre.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error("room insertGameProperties exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public boolean isExistsGameProperty(Long roomId, String key) {
        Connection con = null;
        PreparedStatement pre = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(SELECT_GAMEPROPERTY_VALUE);
            pre.setLong(1, roomId);
            pre.setString(2, key);
            return pre.executeQuery().next();
        } catch (SQLException e) {
            Log.error("room isExistsGameProperty exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public boolean updateGamePropertyById(Long roomId, String value, String key) {
        Connection con = null;
        PreparedStatement pre = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(UPDATE_GAMEPROPERTY_BYROOMIDKEY);
            pre.setString(1, value);
            pre.setLong(2, roomId);
            pre.setString(3, key);
            return pre.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error("room updateGamePropertyById exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    public List<GameProperty> findGameProperty(Long roomId) {
        Connection con = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        List<GameProperty> list = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(SELECT_GAMEPROPERTY_BYROOMID);
            pre.setLong(1, roomId);
            rs = pre.executeQuery();
            list = new ArrayList<GameProperty>();
            while (rs.next()) {
                GameProperty gp = new GameProperty();
                gp.setId(rs.getLong("id"));
                gp.setKey(rs.getString("key"));
                gp.setRoomId(rs.getLong("room_id"));
                gp.setValue(rs.getString("value"));
                list.add(gp);
            }
            return list;
        } catch (SQLException e) {
            Log.error("room findGameProperty  exception: {}", e);
            return list;
        } finally {
            DbConnectionManager.closeConnection(rs, pre, con);
        }
    }

    private String SELECT_PRICE = "SELECT value FROM game_properties g WHERE room_id = ? and g.key = 'prize'";

    public String findLuckyTurntable(Long roomID) {
        Connection con = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(SELECT_PRICE);
            pre.setLong(1, roomID);
            rs = pre.executeQuery();
            while (rs.next()) {
                return rs.getString("value");
            }
            return null;
        } catch (SQLException e) {
            Log.error("room findLuckyTurntable  exception: {}", e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pre, con);
        }
    }

    private String SELECT_ROOM_BYTYPE = "SELECT r.roomID,r.creationDate,r.description,r.maxUsers,r.naturalName,r.roomMark,r.name,r.roomPic,r.subject,r.roomPassword,r.type,r.redPackTotle,r.redPackAmount,r.multiple,r.pardonBack,r.isValidate FROM ofmucroom r WHERE r.type = ? AND selfManagement = ? limit ?,?";

    public MUCRoomEntities findRoomsByType(String type, Integer selfManagement, Integer start, Integer pageSize) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MUCRoomEntity room = null;
        MUCRoomEntities rooms = new MUCRoomEntities();
        List<MUCRoomEntity> list = new ArrayList<MUCRoomEntity>();
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ROOM_BYTYPE);
            pstmt.setString(1, type);
            pstmt.setInt(2, selfManagement);
            pstmt.setInt(3, start);
            pstmt.setInt(4, pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                room = new MUCRoomEntity();
                room.setRoomId(rs.getLong("roomID"));
                room.setRoomName(rs.getString("name"));
                room.setNaturalName(rs.getString("naturalName"));
                room.setDescription(rs.getString("description"));
                room.setMaxUsers(rs.getInt("maxUsers"));
                room.setRoomPic(rs.getString("roomPic"));
                room.setSubject(rs.getString("subject"));
                room.setRoomMark(rs.getLong("roomMark"));
                room.setPassword(rs.getString("roomPassword"));
                room.setType(rs.getString("type"));
                room.setRedPackAmount(rs.getString("redPackAmount"));
                room.setRedPackTotle(rs.getString("redPackTotle"));
                room.setMultiple(rs.getBigDecimal("multiple"));
                list.add(room);
            }
            rooms.setMucRooms(list);
            return rooms;
        } catch (SQLException e) {
            Log.error("room findRoomsByType  exception: {}", e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    private String UPDATE_ROOM_ISGAG = "UPDATE ofmucroom SET isGag = ? WHERE name = ? ";

    public boolean updateRoomIsGag(Integer isGag, String roomName) {
        Connection con = null;
        PreparedStatement pre = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(UPDATE_ROOM_ISGAG);
            pre.setInt(1, isGag);
            pre.setString(2, roomName);
            return pre.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.error("room updateRoomIsGag exception: {}", e);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pre, con);
        }
    }

    private String SELECT_ROOMISGAG = "SELECT isGag FROM ofmucroom WHERE name = ?";

    public Integer selectRoomIsGag(String roomName) {
        Connection con = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pre = con.prepareStatement(SELECT_ROOMISGAG);
            pre.setString(1, roomName);
            rs = pre.executeQuery();
            while (rs.next()) {
                return rs.getInt("isGag");
            }
            return null;
        } catch (SQLException e) {
            Log.error("room findLuckyTurntable  exception: {}", e);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs, pre, con);
        }
    }


}