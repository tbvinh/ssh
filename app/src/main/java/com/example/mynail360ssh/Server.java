package com.example.mynail360ssh;

public class Server {
    private String user;
    private String pwd;
    private String host;
    private String command;

    public Server(String user, String pwd, String host, String command) {
        this.user = user;
        this.pwd = pwd;
        this.host = host;
        this.command = command;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
