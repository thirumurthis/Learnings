///usr/bin/env jbang $0 "$@" ; exit $?

//DEPS org.springframework.boot:spring-boot-dependencies:4.1.0@pom
//DEPS org.springframework.security:spring-security-crypto
//DEPS org.springframework:spring-core
//DEPS commons-logging:commons-logging
//DEPS org.mindrot:jbcrypt:0.4

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptTest {
	
	public static void main (String ... args){
		
		BCryptTest bCryptTest = new BCryptTest();
		
        String rawString = "mySecurePassword123";
        // Example BCrypt hash representing the rawString above
        String storedHash = "$2a$10$R9hbyfE7S8wH.Fp2KEmQYe8Z/BvM.uG0aF9qDbyhW6a/C2I4g1gOq";

		System.out.println("input "+rawString + " generatedHash : "+ bCryptTest.generatePasswordWithJava(rawString));
        System.out.println("[SPRING] Does the string match "+rawString+" the hash? " + bCryptTest.verifyPasswordUsingSpring(rawString, storedHash));
		System.out.println("[JAVA DEP] Does the string match "+ rawString +"the hash? " + bCryptTest.verifyPasswordUsingJava(rawString, storedHash));
		
		rawString = "MySecurePassword123!";
		storedHash = "$2a$11$e0987gX89wP0M1z2y3X4uOE6vB7xY8z9w0v1u2t3s4r5q6p7o8n9m";

		System.out.println("input "+rawString + " generatedHash : "+ bCryptTest.generatePasswordWithJava(rawString));
        System.out.println("[SPRING] Does the string match "+rawString+" the hash? " + bCryptTest.verifyPasswordUsingSpring(rawString, storedHash));
		System.out.println("[JAVA DEP] Does the string match "+ rawString +"the hash? " + bCryptTest.verifyPasswordUsingJava(rawString, storedHash));

	}

	public boolean verifyPasswordUsingJava(String password, String passwordHash) {
		return BCrypt.checkpw(password, passwordHash);
	}
	
	public boolean verifyPasswordUsingSpring(String password, String passwordHash){
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		// Check if the raw string matches the stored hash
        return encoder.matches(password, passwordHash);

	}
	
	public String generatePasswordWithJava(String input){
		// Generate a secure hash with a default workload factor (10)
        return BCrypt.hashpw(input, BCrypt.gensalt());
	}
}