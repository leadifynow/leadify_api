package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.UserColumnsDao;
import com.api.leadify.entity.UserColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserColumnsService {

    private final UserColumnsDao userColumnsDao;

    @Autowired
    public UserColumnsService(UserColumnsDao userColumnsDao) {
        this.userColumnsDao = userColumnsDao;
    }

    public ApiResponse<List<UserColumns>> getUserColumnsByUserIdAndWorkspaceId(Integer userId, String workspaceId) {
        return userColumnsDao.getUserColumnsByUserIdAndWorkspaceId(userId, workspaceId);
    }
    public ApiResponse<UserColumns> updateUserColumns(UserColumns userColumns) {
        return userColumnsDao.updateUserColumns(userColumns);
    }
}
