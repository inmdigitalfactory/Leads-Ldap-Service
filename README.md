# LDAP Authentication Service

This application is a restful service that allows other applications to authenticate their users via ldap without having to integrate LDAP.
Applications that use this service are first registered within the service, with app-specific configuration and given an access token with which they can access the service.