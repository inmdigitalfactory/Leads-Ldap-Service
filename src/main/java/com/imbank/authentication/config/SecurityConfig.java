package com.imbank.authentication.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.config.auth.AllowedAppsAuthenticationFilter;
import com.imbank.authentication.config.auth.jwt.JWTFilter;
import com.imbank.authentication.dtos.ApiResponse;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.repositories.UserRepository;
import com.imbank.authentication.services.AllowedAppService;
import com.imbank.authentication.services.LdapService;
import com.imbank.authentication.utils.AuthUtils;
import com.imbank.authentication.utils.Constants;
import com.imbank.authentication.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.SAMLProcessor;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.storage.EmptyStorageFactory;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.ObjectUtils;

import javax.servlet.Filter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AllowedAppService allowedAppService;

    @Autowired
    private LdapService ldapService;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Value("${security.endpoints.allow}")
    private String[] safeEndpoints;

    @Value("${authentication-service.idp.metadata-url}")
    private String idpMetadataUrl;
    @Value("${authentication-service.entity-id}")
    private String entityId;

    @Value("${authentication-service.domain}")
    private String apiDomain;

    @Value("${authentication-service.entity-base-url}")
    private String entityBaseUrl;
    @Value("${authentication-service.saml-success-url}")
    private String samlSuccessRedirectUrl;
    @Value("${authentication-service.saml-failure-url}")
    private String samlFailureRedirectUrl;

    @Autowired
    private AllowedAppRepository allowedAppRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static SAMLBootstrap samlBootstrap() {
        return new CustomSAMLBootstrap();
    }


    @Bean
    public SAMLContextProviderImpl contextProvider() {
        SAMLContextProviderImpl samlContextProvider = new SAMLContextProviderImpl();
        samlContextProvider.setStorageFactory(emptyStorageFactory());
        return samlContextProvider;
    }



    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(safeEndpoints);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .cors()
                .and()
                .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
                .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(allowedAppsAuthenticationFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(jwtFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling()
                .and()
                .authorizeRequests()
                .antMatchers(safeEndpoints).permitAll()
                .anyRequest().authenticated()
//                .and()
//                .logout()
//                .logoutUrl("/auth/logout")
//                .addLogoutHandler((request, response, authentication) -> {
//                    try {
//                        response.sendRedirect("/saml/logout");
//                    } catch (IOException e) {
//                        log.error("Could not logout", e);
//                    }
//                });
        ;
    }

    @Bean
    public Filter jwtFilter() {
        return new JWTFilter();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(samlAuthenticationProvider());
    }


    public AllowedAppsAuthenticationFilter allowedAppsAuthenticationFilter() throws Exception {
        AllowedAppsAuthenticationFilter filter = new AllowedAppsAuthenticationFilter(allowedAppService, ldapService);
        filter.setFilterProcessesUrl("/auth/login");
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(this::authenticationFailureHandler);
        filter.setAuthenticationSuccessHandler(this::authenticationSuccessHandler);
        log.info("Default auth filter initialized");
        return filter;
    }


    private void authenticationFailureHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.info("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx Error authenticating: {}", e.getLocalizedMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> apiResponse = new ApiResponse<>("-1", e.getLocalizedMessage(), null);
        ObjectMapper objectMapper=new ObjectMapper();
        String r = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().println(r);
    }

    private void authenticationSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        response.setStatus(HttpStatus.OK.value());

        log.info("Login Successful");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        LdapUserDTO ldapUserDTO = (LdapUserDTO) authentication.getCredentials();
        AllowedApp allowedApp = (AllowedApp) authentication.getPrincipal();
        LoginSuccessDTO loginSuccessDTO = AuthUtils.getLoginSuccessDTO(ldapUserDTO, allowedApp);

        ObjectMapper objectMapper=new ObjectMapper();
        String r = objectMapper.writeValueAsString(loginSuccessDTO);

        response.getWriter().println(r);
    }

    /**
     * Filters for processing of SAML messages
     * Filters of the SAML module need to be enabled as part of the Spring Security settings.
     *
     * @return FilterChainProxy
     * @throws Exception
     */
    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), Collections.singletonList(samlEntryPoint())));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), Collections.singletonList(samlLogoutFilter())));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"), Collections.singletonList(metadataDisplayFilter())));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"), Collections.singletonList(samlWebSSOProcessingFilter())));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"), Collections.singletonList(samlLogoutProcessingFilter())));
        return new FilterChainProxy(chains);
    }

    /**
     * Filter processing incoming logout messages -->
     * First argument determines URL user will be redirected to after successful global logout
     *
     * @return SAMLLogoutProcessingFilter
     */
    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    }

    /**
     * Successful authentication using SAML token results in creation of an Authentication object by the SAMLAuthenticationProvider.
     * By default instance of org.springframework.security.providers.ExpiringUsernameAuthenticationToken is created.
     * <p>
     * When forcePrincipalAsString = false AND userDetail = null (default) - NameID object included in the SAML Assertion (credential.getNameID() of type org.opensaml.saml2.core.NameID)
     * When forcePrincipalAsString = false AND userDetail != null - UserDetail object returned from the SAMLUserDetailsService
     *
     * @return SAMLAuthenticationProvider
     */
    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    /**
     * Entry point to initialize authentication, default values taken from properties file
     *
     * @return SAMLEntryPoint
     */
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
        return samlEntryPoint;
    }

    /**
     * After identification of IDP to use for authentication (for details see Section 9.1, “IDP selection and discovery”),
     * SAML Extension creates an AuthnRequest SAML message and sends it to the selected IDP. Both construction of the
     * AuthnRequest and binding used to send it can be customized using WebSSOProfileOptions object. SAMLEntryPoint determines
     * WebSSOProfileOptions configuration to use by calling method getProfileOptions. The default implementation returns the
     * value specified in property defaultOptions. The method can be overridden to provide custom logic for SSO initialization
     *
     * @return WebSSOProfileOptions
     */
    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        webSSOProfileOptions.setNameID(NameIDType.UNSPECIFIED);
        webSSOProfileOptions.setProviderName("Advanced Analytics LDAP Service");
        return webSSOProfileOptions;
    }

    /**
     * Processing filter for WebSSO profile messages
     *
     * @return SAMLProcessingFilter
     * @throws Exception
     */
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(failureRedirectHandler());
        return samlWebSSOProcessingFilter;
    }

    /**
     * Handler deciding where to redirect user after successful login
     *
     * @return SavedRequestAwareAuthenticationSuccessHandler
     */
    @Bean
    public AuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successRedirectHandler.setRedirectStrategy((request, response, url) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            handleSuccessfulLogin(response, auth);
            response.sendRedirect(samlSuccessRedirectUrl);
        });
        successRedirectHandler.setDefaultTargetUrl(samlSuccessRedirectUrl);
        return successRedirectHandler;
    }

    private void handleSuccessfulLogin(HttpServletResponse response, Authentication authentication) {
        log.info("Login successful: Checking details: {}", authentication);
        SAMLCredential credential = (SAMLCredential) authentication.getCredentials();
        String username  = credential.getNameID().getValue();

        Optional<User> user = userRepository.findFirstByUsernameIgnoreCaseAndEnabled(username, true);
        Optional<AllowedApp> thisApp = allowedAppRepository.findFirstByName(Constants.APP_NAME);
        log.info("===================================== User's name: {}\nRemote Entity ID: {}\nAdditionalData: {}\nAttributes: {}", username, credential.getRemoteEntityID(), credential.getAdditionalData(), credential.getAttributes());
        if(user.isPresent() && thisApp.isPresent()) {
            LdapUserDTO ldapUserDTO = LdapUserDTO.builder().user(user.get()).username(username).build();
            String token = JwtUtils.createToken(ldapUserDTO, thisApp.get(), null, false );
            Cookie tokenCookie = new Cookie(Constants.TOKEN_COOKIE_NAME, token);
//                            tokenCookie.setHttpOnly(false);
                    tokenCookie.setPath("/");
                            tokenCookie.setDomain(apiDomain);
                    tokenCookie.setMaxAge((int) thisApp.get().getTokenValiditySeconds());
            String refreshToken = JwtUtils.createToken(ldapUserDTO, thisApp.get(), null, true );
            Cookie refreshTokenCookie = new Cookie(Constants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
//                    refreshTokenCookie.setHttpOnly(false);
            refreshTokenCookie.setPath("/");
                    refreshTokenCookie.setDomain(apiDomain);
            refreshTokenCookie.setMaxAge((int) thisApp.get().getRefreshTokenValiditySeconds());
            response.addCookie(tokenCookie);
            response.addCookie(refreshTokenCookie);
            log.info("Login successful");
        }
        else {
            Map<String, Object> data = Map.of(
                    "message", String.format("User '%s' is not allowed to access this system. Talk to IT Support to get access", username)
            );
            try {
                String value = objectMapper.writeValueAsString(data);
                value = URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
                Cookie loginError = new Cookie("error", value);
                loginError.setPath("/");
                loginError.setDomain(apiDomain);
                loginError.setMaxAge(5);
                response.addCookie(loginError);
            } catch (JsonProcessingException ignored) {}
        }
    }

    /**
     * Handler deciding where to redirect user after failed login
     *
     * @return SimpleUrlAuthenticationFailureHandler
     */
    @Bean
    public SimpleUrlAuthenticationFailureHandler failureRedirectHandler() {
        log.error("Could not authenticate user: ");
        SimpleUrlAuthenticationFailureHandler simpleUrlAuthenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler();
        simpleUrlAuthenticationFailureHandler.setUseForward(false);
        simpleUrlAuthenticationFailureHandler.setDefaultFailureUrl(samlFailureRedirectUrl);
        return simpleUrlAuthenticationFailureHandler;
    }

    /**
     * Override default logout processing filter with the one processing SAML messages
     *
     * @return SAMLLogoutFilter
     */
    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[]{logoutHandler()}, new LogoutHandler[]{logoutHandler()});
    }

    /**
     * Handler for successful logout
     *
     * @return SimpleUrlLogoutSuccessHandler
     */
    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
        simpleUrlLogoutSuccessHandler.setRedirectStrategy((request, response, url) -> {
            log.info("Redirecting after loggedout");
            response.sendRedirect("/login");
        });
        return simpleUrlLogoutSuccessHandler;
    }

    /**
     * Logout handler terminating local session
     *
     * @return SecurityContextLogoutHandler
     */
    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    /**
     * The filter is waiting for connections on URL suffixed with filterSuffix and presents SP metadata there
     *
     * @return MetadataDisplayFilter
     */
    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }

    /**
     * Central storage of cryptographic keys
     *
     * @return JKSKeyManager
     */
    @Bean
    @DependsOn("keystore")
    public JKSKeyManager keyManager() {
        Resource storeFile = new FileSystemResource(new File(Constants.KEYSTORE_FILE_NAME));
        Map<String, String> passwords = new HashMap<>();
        passwords.put(Constants.KEYSTORE_KEY, Constants.KEYSTORE_PASSWORD);
        return new JKSKeyManager(storeFile, Constants.KEYSTORE_PASSWORD, passwords, Constants.KEYSTORE_KEY);
    }

    /**
     * Class loading incoming SAML messages from httpRequest stream
     *
     * @return SAMLProcessor
     */
    @Bean
    public SAMLProcessor processor() {
        return new SAMLProcessorImpl(Arrays.asList(httpPostBinding(), httpRedirectDeflateBinding()));
    }

    /**
     * Logger for SAML messages and events
     *
     * @return SAMLDefaultLogger
     */
    @Bean
    public SAMLDefaultLogger samlLogger() {
        SAMLDefaultLogger samlDefaultLogger = new SAMLDefaultLogger();
        samlDefaultLogger.setLogAllMessages(true);
        samlDefaultLogger.setLogErrors(true);
        samlDefaultLogger.setLogMessagesOnException(true);
        return samlDefaultLogger;
    }

    @Bean
    public EmptyStorageFactory emptyStorageFactory() {
        return new EmptyStorageFactory();
    }

    /**
     * SAML 2.0 Web SSO profile
     *
     * @return WebSSOProfile
     */
    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    /**
     * SAML 2.0 Holder-of-Key Web SSO profile
     *
     * @return WebSSOProfileConsumerHoKImpl
     */
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    /**
     * SAML 2.0 WebSSO Assertion Consumer
     *
     * @return WebSSOProfileConsumer
     */
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        return new WebSSOProfileConsumerImpl();
    }

    /**
     * SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
     *
     * @return WebSSOProfileConsumerHoKImpl
     */
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    /**
     * SAML 2.0 Logout Profile
     *
     * @return SingleLogoutProfile
     */
    @Bean
    public SingleLogoutProfile logoutprofile() {
        return new SingleLogoutProfileImpl();
    }

    /**
     * Filter automatically generates default SP metadata
     *
     * @return MetadataGeneratorFilter
     */
    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(metadataGenerator());
    }

    @Bean
    public MetadataGenerator metadataGenerator() {
        log.info("Entity id is {}", entityId);
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.setEntityId(entityId);
        metadataGenerator.setExtendedMetadata(extendedMetadata());
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setEntityBaseURL(entityBaseUrl);
        metadataGenerator.setKeyManager(keyManager());
        return metadataGenerator;
    }

    @Bean
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false);
        extendedMetadata.setSignMetadata(false);
        return extendedMetadata;
    }

    /**
     * IDP Metadata configuration - paths to metadata of IDPs in circle of trust is here
     *
     * @return CachingMetadataManager
     * @throws MetadataProviderException
     */
    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException {
        List<MetadataProvider> providers = new ArrayList<>();
        providers.add(idpExtendedMetadataProvider());
        return new CachingMetadataManager(providers);
    }

    /**
     * Example of classpath metadata with Extended Metadata
     *
     * @return ExtendedMetadataDelegate
     * @throws MetadataProviderException
     */
    @Bean
    public ExtendedMetadataDelegate idpExtendedMetadataProvider() throws MetadataProviderException {
        ExtendedMetadataDelegate extendedMetadataDelegate;
        if(!ObjectUtils.isEmpty(idpMetadataUrl)) {
            HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(backgroundTimer(), httpClient(), idpMetadataUrl);
            httpMetadataProvider.setParserPool(parserPool());
            extendedMetadataDelegate = new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
        }
        else {
            FilesystemMetadataProvider metadataProvider = new FilesystemMetadataProvider(new File("idp-metadata.xml"));
            metadataProvider.setParserPool(parserPool());
            extendedMetadataDelegate = new ExtendedMetadataDelegate(metadataProvider, extendedMetadata());
        }
        extendedMetadataDelegate.setMetadataTrustCheck(false);
        extendedMetadataDelegate.setMetadataRequireSignature(false);
        return extendedMetadataDelegate;
    }

    @Bean
    public Timer backgroundTimer() {
        return new Timer(true);
    }

    @Bean
    public HttpClient httpClient() {
        return new HttpClient(multiThreadedHttpConnectionManager());
    }

    @Bean
    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
        return new MultiThreadedHttpConnectionManager();
    }


    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }


    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    /**
     * Bindings, encoders and decoders used for creating and parsing messages
     *
     * @return HTTPPostBinding
     */
    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), VelocityFactory.getEngine());
    }


    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }


}
