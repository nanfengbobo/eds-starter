package ch.rasc.eds.starter.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import ch.rasc.eds.starter.entity.User;
import ch.rasc.eds.starter.repository.UserRepository;

@Component
public class JpaUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Autowired
	public JpaUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username);
		if (user != null) {
			return new JpaUserDetails(user);
		}

		throw new UsernameNotFoundException(username);
	}

}
