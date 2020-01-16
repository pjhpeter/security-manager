# 基于Spring Security + JWT的权限管理的简单实践

## 项目介绍

这是一个基于Spring Security + JWT的前后端分离权限管理小例子，用到的框架和数据库如下：
+ Spring Boot 2.2.2
+ Spring Security
+ Spring Data JPA
+ jjwt
+ Ehcache
+ MySQL 8.0.16

主要功能是
+ 用户登录验证后返回token，根据token可以访问其他资源
+ token有效期为30分钟，每次请求成功都会重新刷新token，旧的token就会失效
+ 如果30分钟内没有任何操作，一旦token过期需要重新登录
+ 做了获取当前用户信息和获取当前用户的角色可以访问的资源菜单的简单演示

基于安全的策略
+ 为了防止token被劫持，添加了与客户端MAC地址绑定的机制
+ 为了应对同一用户发送了并发请求，由于token刷新造成的部分请求token验证不通过的情况，将旧的token缓存起来，有效期为3秒

> 项目需要依赖[heart-parent](https://gitee.com/pjhpeter/heart-parent.git)及其子项目**heart-base**两个项目

## 项目如何运行

1. 从gitee上克隆项目
2. 创建MySQL数据库，并运行项目中resources目录中的**security-manager.sql**
3. resources目录下的application.yml配置好数据库链接
4. 运行org.my.heart.SecurityManagerApplication.java启动项目

## 项目如何访问

> 需要安装Postman

### 登录请求

POST请求：127.0.0.1:8080/user/login

data：{"username": "zhangsan", "password": "1234"}

![输入图片说明](https://images.gitee.com/uploads/images/2020/0116/143902_8926ae62_5449551.png "屏幕截图.png")

请求成功，响应头将会返回token

![输入图片说明](https://images.gitee.com/uploads/images/2020/0116/144115_473bc4d6_5449551.png "屏幕截图.png")

![输入图片说明](https://images.gitee.com/uploads/images/2020/0116/144211_7cd68881_5449551.png "屏幕截图.png")

后面的请求，头信息都必须带上这个token才能通过验证。

### 获取用户信息和菜单权限

+ 获取用户信息
GET请求：127.0.0.1:8080/user/info

将登录返回的token放到请求头里面

![输入图片说明](https://images.gitee.com/uploads/images/2020/0116/154821_9d7eff9b_5449551.png "屏幕截图.png")

请求成功会看到下面的效果

![输入图片说明](https://images.gitee.com/uploads/images/2020/0116/155002_bd705714_5449551.png "屏幕截图.png")

响应头会返回新的token

![输入图片说明](https://images.gitee.com/uploads/images/2020/0116/155136_107f9cd0_5449551.png "屏幕截图.png")

下一次请求就要用这个新的token了

+ 获取菜单权限
GET请求：127.0.0.1:8080/role/menu/0

请求的方法跟获取用户信息一样

请求成功会看到下面的效果

![输入图片说明](https://images.gitee.com/uploads/images/2020/0116/155437_2176b23c_5449551.png "屏幕截图.png")

## 代码分析

### 入口

项目启动的时候，Spring Security会运行用注解**@EnableWebSecurity**标注的**配置**类，来配置Spring Security的策略，这个类需要继承**WebSecurityConfigurerAdapter**，并且实现下面两个configure方法。

因此，按照上述规则，创建HeartSecurityConfig类

```java
@Configuration
@EnableWebSecurity
public class HeartSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// 配置AuthenticationManager，全局认证管理对象
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 配置请求过滤规则
	}
}
```

> 所以我们下面自定义的种种验证规则，最后都会通过这个类配置到Spring Security中

### 简单了解Spring Security的登录验证

登录时一般会通过用户名和密码进行验证，Spring Security给出的默认实现是**UsernamePasswordAuthenticationFilter**过滤器，默认拦截/login请求，通过**attemptAuthentication**方法获取用户名和密码，默认是通过**request.getParameter()**，最后将获取到的用户名和密码传到后面的类进行验证。有兴趣的朋友可以自行跟踪源码了解整个机制。

```java
public Authentication attemptAuthentication(HttpServletRequest request,HttpServletResponse response) throws AuthenticationException {
	if (postOnly && !request.getMethod().equals("POST")) {
		throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
	}

	// request.getParameter()方法获取请求中的用户名和密码
	String username = obtainUsername(request);
	String password = obtainPassword(request);

	if (username == null) {
        username = "";
        }

    if (password == null) {
    	password = "";
	}

	username = username.trim();

	UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

	// Allow subclasses to set the "details" property
	setDetails(request, authRequest);

	// 将获取的用户名和密码传递给后续的类进行验证
	return this.getAuthenticationManager().authenticate(authRequest);
}
```

### 改造UsernamePasswordAuthenticationFilter

需要改造的有两点：
1. 前后端分离的情况下，前端会发送POST的Ajax请求到后端，用户名和密码一般是通过request body发送的，request.getParameter()是拿不到的，所以用户名和密码的获取方式需要改变
2. 登录请求地址不一定是/login，可以自定义

首先处理第1点，下面是创建一个自定义的过滤器**RequestBodyUsernamePasswordAuthenticationFilter**，继承**UsernamePasswordAuthenticationFilter**，并重写**attemptAuthentication**方法

```java
public class RequestBodyUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestBodyUsernamePasswordAuthenticationFilter.class);

	@Autowired
	private ExceptionResovler exceptionResovler;
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		try {
			// 从request body中获取前端发送的用户名和密码
			LoginUser loginUser = new ObjectMapper().readValue(request.getInputStream(), LoginUser.class);
			if (loginUser == null) {
				log.error("账号不存在");
				exceptionResovler.handleAuthenticationException(response, new AuthenticationCredentialsNotFoundException("账号不存在"));
				return null;
			}
			log.debug("获取到用户名：" + loginUser.getUsername() + "，密码：" + loginUser.getPassword());
			// 下面的逻辑就跟UsernamePasswordAuthenticationFilter一样了
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword());
			return this.getAuthenticationManager().authenticate(authRequest);
		} catch (Exception e) {
			log.error("登录异常，错误：" + e.getMessage());
			try {
				exceptionResovler.handleAccessDeniedException(response, new AuthorizationServiceException("登录异常，请与管理员联系"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
}
```

处理第2点，在**HeartSecurityConfig**中注入**RequestBodyUsernamePasswordAuthenticationFilter**，实例化的时候可以自定义拦截的登录路径

```java
@Configuration
@EnableWebSecurity
public class HeartSecurityConfig extends WebSecurityConfigurerAdapter {

	// 登录验证过滤器
	@Bean
	public UsernamePasswordAuthenticationFilter requestBodyUsernamePasswordAuthenticationFilter() throws Exception {
		RequestBodyUsernamePasswordAuthenticationFilter filter = new RequestBodyUsernamePasswordAuthenticationFilter();
		// 不指定拦截路径就算替换了UsernamePasswordAuthenticaitonFilter都不会进来
		filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/user/login", HttpMethod.POST.name()));
		// 自定义UsernamePasswordAuthenticationFilter就会导致原来configure中的.successHandler和.failureHandler失效
		filter.setAuthenticationSuccessHandler(jwtAuthenticationSuccessHandler);
		filter.setAuthenticationFailureHandler(jwtAuthentiacionFailureHandler);
		// 自定义filter必须指定AuthenticationManager
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// 配置AuthenticationManager，全局认证管理对象
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 配置请求过滤规则
	}
}
```

## 从数据库中读取用户信息与前端用户名和密码对比

从数据库读取用户信息，Spring Security提供了默认的实现，**JdbcUserDetailsManager**，不过每个系统都有自己的业务逻辑，所以一般都会自定义一个，Spring Security给我们提供了**UserDetailsService**接口，只要实现这个接口，即可自定义自已的业务逻辑。

这里创建了**JWTUserDetailsService**，通过前端传递的username从数据库读取用户信息。返回的用户信息对象**JWTUser**则需要实现**UserDetails**接口，可以根据自己的业务需要来定义用户信息的成员变量。

**JWTUser**

```java
public class JWTUser implements UserDetails {

	private static final long serialVersionUID = -8259845930972485319L;

	private Long id;

	private String username;

	private String name;

	private String password;

	private List<GrantedAuthority> authorities;
	
	private Boolean nonLock;

	private Boolean gredentialsNonExpired;

	private Boolean enabled;
	
	private String macAddress;

	public static JWTUser build() {
		return new JWTUser();
	}

	public Long getId() {
		return id;
	}

	public JWTUser setId(Long id) {
		this.id = id;
		return this;
	}

	public JWTUser setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getName() {
		return name;
	}

	public JWTUser setName(String name) {
		this.name = name;
		return this;
	}

	public JWTUser setPassword(String password) {
		this.password = password;
		return this;
	}

	public JWTUser setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
		return this;
	}

	public JWTUser setNonLock(Boolean nonLock) {
		this.nonLock = nonLock;
		return this;
	}

	public JWTUser setGredentialsNonExpired(Boolean gredentialsNonExpired) {
		this.gredentialsNonExpired = gredentialsNonExpired;
		return this;
	}

	public JWTUser setEnabled(Boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.nonLock;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.gredentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public JWTUser setMacAddress(String macAddress) {
		this.macAddress = macAddress;
		return this;
	}

}
```

**JWTUserDetailsService**

```java
@Service("jwtUserDetailsService")
public class JWTUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// 通过username查询对应的用户信息，下面是JPA的条件查询写法
		Optional<User> userOptional = userRepository.findOne(new Specification<User>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				Predicate equal = criteriaBuilder.equal(root.get("username"), username);
				return equal;
			}
		});
		User user = userOptional.get();
		
		// 生成用户信息，提供给Spring Security进行校验
		JWTUser jwtUser = JWTUser.build()
									.setId(user.getId())
									.setUsername(username)
									.setName(user.getName())
									.setPassword(user.getPassword())
									.setNonLock(user.getNonLock())
									.setGredentialsNonExpired(user.getGredentialsNonExpired())
									.setEnabled(user.getEnabled());
		// 读取角色数据
		List<Role> roles = user.getRoles();
		if (roles != null && roles.size() > 0) {
			String[] roleIdArr = new String[roles.size()];
			for (int i = 0; i < roles.size(); i++) {
				roleIdArr[i] = roles.get(i).getId().toString();
			}
			jwtUser.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils.join(roleIdArr, ",")));
		}
		return jwtUser;
	}

}
```

## 自定义密码加密算法

Spring Security会使用定义好的加密算法，将前端传来的密码加密,然后跟数据库中读取到的用户信息中的密码进行对比，所以数据库中存的密码是经过加密后的字符串。

至于用什么加密算法，需要我们先声明和注入到Spring容器中。在**HeartSecurityConfig**中注入加密处理类，这里使用加盐算法：

```java
// 全局加密算法
@Bean
public PasswordEncoder getPasswordEncoder() {
	// 加盐算法
	return new BCryptPasswordEncoder();
}
```

## 自定义登录验证成功和失败处理

登录的验证Spring Security底层已帮我们做好，成功和失败的处理也有默认实现，不过我们一般都要根据自己的业务来定义处理逻辑，我们需要定义两个类，实现**AuthenticationSuccessHandler**和**AuthenticationFailureHandler**。

项目结合的是JWT，所以成功是会返回一个JWT规范的token，失败则返回错误信息。这里创建**JWTAuthenticationSuccessHandler**和**JWTAuthentiacionFailureHandler**：

**JWTAuthenticationSuccessHandler**

```java
@Component("jwtAuthenticationSuccessHandler")
public class JWTAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationSuccessHandler.class);

	@Autowired
	private JWTTokenHandler jwtTokenHandler;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		// Spring Security会将用户信息传递过来
		JWTUser jwtUser = (JWTUser) authentication.getPrincipal();

		// 生成JWT规范的token，绑定客户端MAC地址
		String ipAddress = IpUtils.getIpAddress(request);
		String macAddress = IpUtils.getMacAddress(ipAddress);
		// 按照规范，token会响应到Authorization头信息中
		response.setHeader(JWTTokenHandler.TOKEN_HEADER_NAME, jwtTokenHandler.buildToken(jwtUser.setMacAddress(macAddress)));
		ResponseUtils.buildResponseBody(response, Result.ok("登录成功"));
		log.debug("登录成功");
	}

}
```

> 其中**JWTTokenHandler**类封装了所有token的操作方法

**JWTAuthentiacionFailureHandler**

```java
@Component("jwtAuthentiacionFailureHandler")
public class JWTAuthentiacionFailureHandler implements AuthenticationFailureHandler {

	private static final Logger log = LoggerFactory.getLogger(JWTAuthentiacionFailureHandler.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		ResponseUtils.buildResponseBody(response, Result.failure(HttpStatus.UNAUTHORIZED.value()).setMessage(exception.getMessage()));
		log.debug("登录失败");
	}

}
```

> 至此整个登录验证过程结束了

## 根据token验证请求

登录验证成功之后，响应token到前端，前端根据这个token来访问其他资源，所以需要定义一个过滤器来验证token的有效性，为了确保这个过滤器每个请求都会触发一次，所以需要继承**OncePerRequestFilter**。

> 当请求被登录验证过滤器拦截后，OncePerRequestFilter不会被触发

这里创建**JWTAuthenticationFilter**进行token验证：

```java
@Component("jwtAuthenticationFilter")
public class JWTAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JWTTokenHandler jwtTokenHandler;
	
	@Autowired
	private ExceptionResovler exceptionResovler;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// 获取前端传来的token
		String token = jwtTokenHandler.getToken(request);
		if (StringUtils.isBlank(token)) {
			exceptionResovler.handleAuthenticationException(response, new AuthenticationCredentialsNotFoundException("请登录"));
			return;
		}
		try {
			// 解析token，如果token解析失败，这句代码会根据失败的类型抛出不同的异常，所以解析token即是验证token的合法性
			JWTUser jwtUser = jwtTokenHandler.parseToken(token);
			// 从缓存中读取最新的token
			String tokenFromCache = jwtTokenHandler.getTokenFromCache(jwtUser.getId());
			// 判断这个token是否失效
			if(!token.equals(tokenFromCache)) {
				// 前端传来的可能是老的token，从缓存中读取为过期的老token
				tokenFromCache = jwtTokenHandler.getTokenFromOldCache(token);
				if(StringUtils.isBlank(tokenFromCache)) {
					// 如果缓存中没有这个老token，说明已经失效了
					exceptionResovler.handleAuthenticationException(response, new CredentialsExpiredException("令牌已失效"));
					return;
				}
			}
			// 判断请求方的mac地址是否也token里的一致，防止劫持token发起的攻击
			if (jwtUser.getMacAddress().equals(IpUtils.getMacAddress(IpUtils.getIpAddress(request)))) {
				// token通过验证
				authorizeSuccess(token, jwtUser, response);
				filterChain.doFilter(request, response);
			} else {
				// token被劫持
				exceptionResovler.handleAuthenticationException(response, new BadCredentialsException("这样做是犯法的哦^_^"));
				return;
			}
		} catch (ExpiredJwtException e) {
			// 超过了30分钟，token过期
			exceptionResovler.handleAuthenticationException(response, new CredentialsExpiredException("令牌过期，请重新登录"));
			return;
		} catch (SignatureException e) {
			// token被篡改
			exceptionResovler.handleAuthenticationException(response, new BadCredentialsException("这样做是犯法的哦^_^"));
			return;
		}catch (MalformedJwtException e) {
			// token格式异常
			exceptionResovler.handleAuthenticationException(response, new BadCredentialsException("这样做是犯法的哦^_^"));
			return;
		}
	}
	
	/*
	 * token验证成功
	 */
	private void authorizeSuccess(String oldToken, JWTUser jwtUser, HttpServletResponse response) throws IOException {
		// 产生新token
		String newToken = jwtTokenHandler.buildToken(jwtUser);
		
		// 以老token为key缓存新token，期限为3秒，防止同时多个异步请求
		String tokenFromCache = jwtTokenHandler.getTokenFromOldCache(oldToken);
		if(StringUtils.isBlank(tokenFromCache)) {
			jwtTokenHandler.cacheOldToken(oldToken, newToken);
		}
		
		// 将新token写到响应头信息中
		response.setHeader(JWTTokenHandler.TOKEN_HEADER_NAME, newToken);
	}

}
```

> 只有通过了这个过滤器后，请求才能到达controller层

## 将自定义策略配置到Spring Security

上面自定义的策略需要在**HeartSecurityConfig**里配置才能生效，**HeartSecurityConfig**完整代码如下：

```java
@Configuration
@EnableWebSecurity
public class HeartSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService jwtUserDetailService;

	@Autowired
	private AuthenticationSuccessHandler jwtAuthenticationSuccessHandler;

	@Autowired
	private AuthenticationFailureHandler jwtAuthentiacionFailureHandler;

	@Autowired
	private OncePerRequestFilter jwtAuthenticationFilter;

	// 全局加密算法
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 登录验证过滤器
	@Bean
	public UsernamePasswordAuthenticationFilter requestBodyUsernamePasswordAuthenticationFilter() throws Exception {
		RequestBodyUsernamePasswordAuthenticationFilter filter = new RequestBodyUsernamePasswordAuthenticationFilter();
		// 不指定拦截路径就算替换了UsernamePasswordAuthenticaitonFilter都不会进来
		filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/user/login", HttpMethod.POST.name()));
		// 自定义UsernamePasswordAuthenticationFilter就会导致原来configure中的.successHandler和.failureHandler失效
		filter.setAuthenticationSuccessHandler(jwtAuthenticationSuccessHandler);
		filter.setAuthenticationFailureHandler(jwtAuthentiacionFailureHandler);
		// 自定义filter必须指定AuthenticationManager
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}

	/**
	 * 配置AuthenticationManager，全局认证管理对象
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// 配置从数据库读取用户信息的自定义实现
		auth.userDetailsService(jwtUserDetailService);
	}

	/**
	 * 配置请求过滤规则
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 表单登录
		http.formLogin().and()
				// 替换默认的用户名密码验证过滤器UsernamePasswordAuthenticationFilter
				.addFilterAt(requestBodyUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				// JWT验证不需要session
				.sessionManagement().disable()
				// 没有session机制则不需要csrf防护
				.csrf().disable()
				// 添加权限验证过滤器
				.addFilterAfter(jwtAuthenticationFilter, RequestBodyUsernamePasswordAuthenticationFilter.class);
	}
}
```

> 整个项目的主要业务逻辑到这里结束了，业务细节可以通过阅读源码了解，再见^_^