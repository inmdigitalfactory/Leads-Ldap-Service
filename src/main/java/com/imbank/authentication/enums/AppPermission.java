package com.imbank.authentication.enums;

public enum AppPermission {
    searchUser,
    createApp,
    updateApp,
    resetAppToken,
    deleteApp,
    getAllApps,
    getApp, //specific to app

    createUser,//specific to app
    updateUser,//specific to app
    deleteUser,//specific
    getUser,//specific
    getAllUsers,

    getRoles,//specific
    addRole,
    updateRole,
    setRolePermissions, getPermissions, addPermission, viewAuditLogs, deleteRole
}
