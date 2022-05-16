package com.imbank.authentication.utils;

import com.imbank.authentication.entities.AuditLog;
import com.imbank.authentication.enums.AuditAction;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.imbank.authentication.enums.AuditAction.*;

@Slf4j
public class Utils {


    public static List<AuditAction> getActions(String action) {
        List<AuditAction> actions ;
        action = (action+"").toLowerCase();

        if("create".startsWith(action)) {
            actions = List.of(addSystemAccess, addUser, createApp);
        }
       else if ("update".startsWith(action)) {
           actions = List.of(updateUser);
        }
       else if ("delete".startsWith(action)) {
           actions = List.of(deleteUser);
        }
       else {
            actions = Collections.emptyList();
        }

        log.info("Actions are {}", actions);
        return actions;
    }

    public static String mapCrud(AuditAction action) {
        String act = action.name().toLowerCase(Locale.ROOT);
        if(act.startsWith("create") || act.startsWith("add")) {
            return "CREATE";
        }
        if(act.startsWith("update") || act.startsWith("modify")) {
            return "UPDATE";
        }
        if(act.startsWith("delete") || act.startsWith("remove")) {
            return "DELETE";
        }
        return action.name();
    }

    public static String mapCrudDescription(AuditLog auditLog) {
        switch (auditLog.getAction()) {
            case updateUser:
                return String.format("Updated user %s information", auditLog.getUserName());
            case addSystemAccess:
                return String.format("Added access for %s to %s", auditLog.getAppName(), auditLog.getUserName());
            case deleteUser:
                return String.format("Deleted user %s", auditLog.getUserName());
            case createApp:
                return String.format("Created an application %s", auditLog.getAppName());
            case addUser:
                return String.format("Added user %s to LDAP Service", auditLog.getUserName());
        }
        return "";
    }

}
