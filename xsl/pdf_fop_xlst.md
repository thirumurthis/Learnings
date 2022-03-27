##3 Below is the code demonstration for generting PDF with the XSL file using `Apache FO` project.

- The code below generates, pdf using the XML content from a String variable.
 - This demonstrates the fact that we can fetch the data from Database, construct the XML using the data
 - The XML content can be fetched from file as well.

##### Sample XML content
```
<Report>
<Title>.. </Title>
<For>..</For>
<Employees>
  <Employee>
    <Name>...</Name>
    <Department>..</Department>
  </Employee>
</Employees>
</Report>
```

- XSL template definition file:
  - This file is generated using the IDE itself, not tools used.
  - Along with the XSL stylesheet tags, we also use `fop` namespace tags.
  - The values of the XML is fetched using the XPATH format
  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">


	<xsl:variable name="topic" select="/Report/Title" />
	<xsl:output method="xml" />

	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master margin-right="2.0cm"
					margin-left="2.0cm" margin-bottom="2.0cm" margin-top="2.0cm"
					page-height="29.7cm" page-width="21cm" master-name="test">
					<fo:region-body margin-top="2.5cm" margin-bottom="1.0cm" />
					<fo:region-before extent="5cm" />
					<fo:region-after extent="1cm" />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<!-- master-name should match the master-name in the simple-page-master -->
			<fo:page-sequence master-reference="test">
				<xsl:text>&#10;</xsl:text>

				<fo:static-content flow-name="xsl-region-before">
					<xsl:text>&#10;</xsl:text>
					<!-- <fo:table><xsl:text>&#10;</xsl:text> -->
					<fo:table table-layout="fixed" width="100%" border-collapse="separate">
						<fo:table-column column-width="7.5cm" /><xsl:text>&#10;</xsl:text>
						<fo:table-column column-width="8.5cm" /><xsl:text>&#10;</xsl:text>
						<fo:table-body><xsl:text>&#10;</xsl:text>
							<fo:table-row><xsl:text>&#10;</xsl:text>
								<fo:table-cell border="0.5pt solid white"> <!-- No border is needed so white -->
									<fo:block line-height="15pt" font-size="10pt"
										font-weight="bold" text-align="center" space-after="5mm">
										<xsl:text>Report Name: </xsl:text><xsl:value-of select="/Report/Title" />
									</fo:block><xsl:text>&#10;</xsl:text>
								</fo:table-cell><xsl:text>&#10;</xsl:text>
								<fo:table-cell border="0.5pt solid white"> <!-- No border is needed so white -->
									<fo:block line-height="15pt" font-size="10pt"
										font-weight="bold" text-align="center" space-after="5mm">
										<xsl:text>Used For: </xsl:text><xsl:value-of select="/Report/For" />
									</fo:block><xsl:text>&#10;</xsl:text>
								</fo:table-cell><xsl:text>&#10;</xsl:text>
							</fo:table-row>
						</fo:table-body>
					</fo:table>
				</fo:static-content>
				<!-- Flow name is required part of the validation -->
				<fo:flow flow-name="xsl-region-body">
					<fo:table table-layout="fixed" width="100%" border-collapse="separate">
						<fo:table-column column-width="7.5cm" /><xsl:text>&#10;</xsl:text>
						<fo:table-column column-width="7.5cm" /><xsl:text>&#10;</xsl:text>
						<!-- <xsl:call-template name=""></xsl:call-template> -->
						<fo:table-body><xsl:text>&#10;</xsl:text>
							<fo:table-row><xsl:text>&#10;</xsl:text>
								<fo:table-cell border="0.5pt solid black" height="15pt">
									<fo:block line-height="10pt" font-size="10pt"
										font-weight="bold" text-align="center" vertical-align="middle"
										space-after="5mm" space-before="5mm">
										<xsl:text>Employee Name</xsl:text>
									</fo:block><xsl:text>&#10;</xsl:text>
								</fo:table-cell><xsl:text>&#10;</xsl:text>
								<fo:table-cell border="0.5pt solid black" height="15pt">
									<fo:block line-height="10pt" font-size="10pt"
										font-weight="bold" text-align="center" vertical-align="middle"
										space-after="5mm" space-before="5mm">

										<xsl:text> Department </xsl:text>
									</fo:block><xsl:text>&#10;</xsl:text>
								</fo:table-cell><xsl:text>&#10;</xsl:text>
							</fo:table-row>
							<xsl:apply-templates select="Report/Employees" />
						</fo:table-body>
					</fo:table>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	<xsl:template match="/Report/Employees">
		<xsl:for-each select="./Employee">
			<fo:table-row>
				<fo:table-cell border="0.5pt solid black">
					<fo:block font-size="8pt" text-align="center" vertical-align="middle">
						<xsl:value-of select="Name" />
					</fo:block>
				</fo:table-cell>
				<fo:table-cell border="0.5pt solid black">
					<fo:block font-size="8pt" text-align="center" vertical-align="middle">
						<xsl:value-of select="Department" />
					</fo:block>
				</fo:table-cell>
			</fo:table-row>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
```

- Java main code to generate the PDF
  - Two apporach is defined
    - 1. Without the FOPUserAgent
    - 2. With the FOPUserAgent
    
```java
package xsltdemo.xsltdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.xalan.xsltc.trax.TransformerFactoryImpl;
import org.xml.sax.SAXException;

public class App {
    
    public static void main(String[] args) {
    	System.out.println("Process Started");
    	createPDF();
    	createPDFPerDoc();
    	System.out.println("Process Completed");
    }
 
    public static void createPDF() {

    	/**************************************************************
    	 ****  https://xmlgraphics.apache.org/fop/2.7/embedding.html **
    	 ****  https://xmlgraphics.apache.org/fop/fo.html            **
    	 **************************************************************/
  
    	
    	//Template file created for XSLT transformation
    	//This include fop namespace used for PDF creation
    	//In the IDE, i created a src folder resources and kept the file
    	String templateXSL ="resources/employee-pdf.xsl";
    	//Fetching the content from the variable within the class.
    	//we can also read the content from file
    	//For reading content from file, we can pass File object in StreamSource 
    	// instead of string
    	String templateXMLContent = createXml();
    	try {

    		TransformerFactory transformerFactory = new TransformerFactoryImpl();
    		
    		//If we are reading the content from file, use new File("file-path-of-xml");
    		//instead of string in StreamSource
    		Transformer transformer = transformerFactory.newTransformer(new StreamSource(templateXSL));

    		//The output stream for the PDF to be generated
    		OutputStream out = new FileOutputStream("test-demo.pdf");
    		
    		//We can specify the fo config file, in new Instance, so FOP factory can use it
    		// In our case we are simply setting the setting current working directory as base for configuration
    		FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
    		    		 
    		//Setting the desired output format
    		Fop fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, out);
    		
    		//provide the fop handler for SAX parser to handle fop events
    		Result res = new SAXResult(fop.getDefaultHandler());
    		
    		//actual XSLT transform using FOP happens here
    		transformer.transform(new StreamSource(new StringReader(templateXMLContent)),res);
    		
    	} catch (SAXException | IOException | TransformerException e) {
    		e.printStackTrace();
    	}
    }
    
    public static String createXml() {
    	
    	String str ="";
    	
    	str = "<Report>"
    			+ "<Title>Employee Details</Title>"
    			+ "<For>Department Info</For>"
    			+"<Employees>"
    			+"<Employee>"
    			+"<Name>Ram</Name>"
    			+"<Department>Finance</Department>"
    			+ "</Employee>"
    			+"<Employee>"
    			+"<Name>Tim</Name>"
    			+"<Department>Sale</Department>"
    			+ "</Employee>"
    			+ "</Employees>"
    			+ "</Report>";
    	return str;
    }
    /*
      Uses FOPUserAgent
    */
    public static void createPDFPerDoc() {
    	OutputStream out;
    	 FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
        	String templateXSL ="resources/employee-pdf2.xsl";
            //Load the stylesheet
            Templates templates = tFactory.newTemplates(
                new StreamSource(new File(templateXSL)));

            //First run (to /dev/null)
            out = new org.apache.commons.io.output.NullOutputStream();
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, foUserAgent, out);
            Transformer transformer = templates.newTransformer();
            transformer.setParameter("page-count", "#");
            transformer.transform(new StreamSource(new StringReader(createXml())),
                new SAXResult(fop.getDefaultHandler()));

            //Get total page count
            //String pageCount = Integer.toString(Driver.getResults().getPageCount());
            String pageCount ="2"; 

            //Second run (the real thing)
            out = new java.io.FileOutputStream("test-demo-2.pdf");
            out = new java.io.BufferedOutputStream(out);
            try {
              foUserAgent = fopFactory.newFOUserAgent();
              fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, foUserAgent, out);
              transformer = templates.newTransformer();
              transformer.setParameter("page-count", pageCount);
              transformer.transform(new StreamSource(new StringReader(createXml())),
                  new SAXResult(fop.getDefaultHandler()));
            } finally {
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
- Dependencies for the code `pom.xml`
  - Note: I excluded xml-apis, since the `javaxml.sax` pacakge in java 11 displayed a module conflict.
  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
 
    <groupId>xsltdemo</groupId>
    <artifactId>xsltdemo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>
 
    <name>xsltdemo</name>
    <url>http://www.example.com</url>
 
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>11</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <maven.compiler.release>${java.version}</maven.compiler.release>

        <junit>5.6.2</junit>
        
        <!-- Plugin versions -->
        <maven.shade>3.2.2</maven.shade>
        <maven.clean>3.1.0</maven.clean>
        <maven.resources>3.1.0</maven.resources>
        <maven.compiler>3.8.1</maven.compiler>
        <maven.surefire>3.0.0-M5</maven.surefire>
        <maven.jar>3.2.0</maven.jar>
        <maven.install>3.0.0-M1</maven.install>
    </properties>

    <dependencies>
        <!-- Dependencies -->
        <dependency>
            <groupId>org.apache.xmlgraphics</groupId>
            <artifactId>fop</artifactId>
            <version>2.7</version>
            <exclusions>
              <!-- Exclucsion done for fixing javaxml.sax issue module conflict issue-->
               <exclusion>
                  <groupId>xml-apis</groupId>
                   <artifactId>xml-apis</artifactId>
              </exclusion>
         </exclusions> 
        </dependency>   
    
        <!-- Testing dependencies-->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>${junit}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-clean-plugin</artifactId>
                <version>3.1.0</version>
            </plugin>
            <plugin>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.1.0</version>
            </plugin>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
            </plugin>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M4</version>
            </plugin>
            <plugin>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
            </plugin>
            <plugin>
                <artifactId>maven-install-plugin</artifactId>
                <version>3.0.0-M1</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>${maven.shade}</version>
                <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                    <goal>shade</goal>
                    </goals>
                    <configuration>
                    <transformers>
                        <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>xsltdemo.xsltdemo.App</mainClass>
                        </transformer>
                    </transformers>
                    </configuration>
                </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
