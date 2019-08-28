package com.amrit.service;


import com.amrit.beans.User;

public interface UserService {
    void save(User user);

    User findUserByUsername(String username);
}
