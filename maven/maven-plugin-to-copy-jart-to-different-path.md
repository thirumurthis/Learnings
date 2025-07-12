
Add below content to the pom.xml

```xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<version>3.8.1</version>
				<executions>
					<execution>
						<id>copy</id>
						<phase>package</phase>
						<goals>
							<goal>copy</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<artifactItems>
						<artifactItem>
							<groupId>${project.groupId}</groupId>
							<artifactId>${project.artifactId}</artifactId>
							<version>${project.version}</version>
							<type>${project.packaging}</type>
						</artifactItem>
					</artifactItems>
					<outputDirectory>C:\\thiru\\edu\\AI-mcp\\jar</outputDirectory>
				</configuration>
			</plugin>
		</plugins>
	</build>
```
