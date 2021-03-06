package ch.rasc.eds.starter.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.db.DBAppender;
import ch.qos.logback.core.db.DataSourceConnectionSource;
import ch.rasc.eds.starter.entity.Role;
import ch.rasc.eds.starter.entity.User;
import ch.rasc.eds.starter.repository.UserRepository;

@Component
class Startup {

	private final UserRepository userRepository;

	private final DataSource dataSource;

	private final PasswordEncoder passwordEncoder;

	@Autowired
	public Startup(UserRepository userRepository, DataSource dataSource,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.dataSource = dataSource;
		this.passwordEncoder = passwordEncoder;
		init();
	}

	public void init() {

		configureLog();

		if (userRepository.count() == 0) {
			// admin user
			User adminUser = new User();
			adminUser.setEmail("admin@starter.com");
			adminUser.setFirstName("admin");
			adminUser.setLastName("admin");
			adminUser.setLocale("en");
			adminUser.setPasswordHash(passwordEncoder.encode("admin"));
			adminUser.setEnabled(true);
			adminUser.setRole(Role.ADMIN.name());
			userRepository.save(adminUser);

			// normal user
			User normalUser = new User();
			normalUser.setEmail("user@starter.com");
			normalUser.setFirstName("user");
			normalUser.setLastName("user");
			normalUser.setLocale("de");
			normalUser.setPasswordHash(passwordEncoder.encode("user"));
			normalUser.setEnabled(true);
			normalUser.setRole(Role.USER.name());
			userRepository.save(normalUser);
		}

	}

	private void configureLog() {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);

		if (logger.getAppender("DB") == null) {
			DBAppender appender = new DBAppender();
			appender.setName("DB");
			appender.setContext(lc);

			DataSourceConnectionSource cs = new DataSourceConnectionSource();
			cs.setDataSource(dataSource);
			cs.setContext(lc);
			cs.start();

			appender.setConnectionSource(cs);
			appender.start();

			logger.addAppender(appender);
		}

	}

}
