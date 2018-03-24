package com.doowal.offlinepush.entity;

public class MsgPushToken
{
  private Long id;
  private String username;
  private String token;
  
  public Long getId()
  {
    return this.id;
  }
  
  public void setId(Long id)
  {
    this.id = id;
  }
  
  public String getUsername()
  {
    return this.username;
  }
  
  public void setUsername(String username)
  {
    this.username = username;
  }
  
  public String getToken()
  {
    return this.token;
  }
  
  public void setToken(String token)
  {
    this.token = token;
  }
}
