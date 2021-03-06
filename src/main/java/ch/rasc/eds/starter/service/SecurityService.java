package ch.rasc.eds.starter.service;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.rasc.eds.starter.config.security.JpaUserDetails;
import ch.rasc.eds.starter.entity.AccessLog;
import ch.rasc.eds.starter.entity.User;
import ch.rasc.eds.starter.repository.AccessLogRepository;
import ch.rasc.eds.starter.repository.UserRepository;

@Service
public class SecurityService {
	private final static UserAgentStringParser UAPARSER = UADetectorServiceFactory
			.getResourceModuleParser();

	private final UserRepository userRepository;

	private final AccessLogRepository accessLogRepository;

	private final GeoIPCityService geoIpCityService;

	@Autowired
	public SecurityService(UserRepository userRepository,
			AccessLogRepository accessLogRepository, GeoIPCityService geoIpCityService) {
		this.geoIpCityService = geoIpCityService;
		this.userRepository = userRepository;
		this.accessLogRepository = accessLogRepository;
	}

	@ExtDirectMethod
	@PreAuthorize("isAuthenticated()")
	public User getLoggedOnUser(HttpServletRequest request, HttpSession session,
			@AuthenticationPrincipal JpaUserDetails jpaUserDetails) {

		if (jpaUserDetails != null) {
			User user = userRepository.findOne(jpaUserDetails.getUserDbId());
			if (jpaUserDetails.hasRole("ADMIN")) {
				user.setAutoOpenView("Starter.view.accesslog.TabPanel");
			} else if (jpaUserDetails.hasRole("USER")) {
				user.setAutoOpenView("Starter.view.dummy.View");
			}			
			insertAccessLog(request, session, user);
			return user;
		}

		return null;
	}

	private void insertAccessLog(HttpServletRequest request, HttpSession session,
			User user) {

		AccessLog accessLog = new AccessLog();
		accessLog.setEmail(user.getEmail());
		accessLog.setSessionId(session.getId());
		accessLog.setLogIn(LocalDateTime.now());

		String ipAddress = request.getHeader("X-Forwarded-For");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		accessLog.setIpAddress(ipAddress);
		accessLog.setLocation(geoIpCityService.lookupCity(ipAddress));

		String ua = request.getHeader("User-Agent");
		if (StringUtils.hasText(ua)) {
			accessLog.setUserAgent(ua);
			ReadableUserAgent agent = UAPARSER.parse(ua);
			accessLog.setUserAgentName(agent.getName());
			accessLog.setUserAgentVersion(agent.getVersionNumber().getMajor());
			accessLog.setOperatingSystem(agent.getOperatingSystem().getFamilyName());
		}

		accessLogRepository.save(accessLog);
	}

	@ExtDirectMethod
	@PreAuthorize("hasRole('ADMIN')")
	public boolean switchUser(Long userId) {
		User switchToUser = userRepository.findOne(userId);
		if (switchToUser != null) {

			JpaUserDetails principal = new JpaUserDetails(switchToUser);
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
					principal, null, principal.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(token);

			return true;
		}

		return false;
	}

}
