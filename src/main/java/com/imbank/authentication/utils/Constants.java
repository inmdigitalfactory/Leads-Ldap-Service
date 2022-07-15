package com.imbank.authentication.utils;

public class Constants {
    public static final String ISSUER = "auth-app";

    public static final String FUNCTION_TOKEN = "token";
    public static final String FUNCTION_REFRESH_TOKEN = "refresh_token";
    public static final String CLAIM_USER = "user";
    public static final String CLAIM_APP = "app";
    public static final String CLAIM_IP = "ip";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_APPS = "apps";
    public static final String CLAIM_FUNCTION = "function";


    public static final String APP_NAME = "Authentication Service";
    public static final String KEYSTORE_KEY = "saml";
    public static final String KEYSTORE_PASSWORD = "imbank";
    public static final String KEYSTORE_FILE_NAME = "ldapserver.jks";
    public static final int PAGE_SIZE = 15;
    public static final String TOKEN_COOKIE_NAME = "token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
}
