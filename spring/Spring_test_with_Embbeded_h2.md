## Spring testing using the RunWith, junit 4 version

- Create an context xml where the bean is defined (xml or java configuration), i am using an xml


use below annotation in the class, if we have the context file created for each module or class or functionality
and stored those xml under resource/package_path/text_context.xml

```java
# annotate the class with 
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/package_path/text_context.xml"}) //the reference to the xml

public class TestEmbTest{

private EmbeddedDataSource datasource;
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private ProducateDao productDao; // this is the DAO layer or the Repository 
  
  /*
  IMPORTANT: Make sure to add the DELETE statements to delete the data in the script.
  Though H2 is memory that is not required, if we are using Oracle/ other temp table pay attention there.
  */
@Before
public void setUp(){
   datasource = new EmbeddedDatabaseBuilder()
       .setType(EmbeddedDatabaseType.H2)
       .addScript("package/where/the/sample/sql/script/data.sql") // path to the sql data selt
       .addScript("package/where/the/sample/sql/script/data.sql") // path to the sql data selt
       .buld();
  
  productDao.setDataSource(dataSource); // Note, ProductDao.java -> has the datasource injected using setter
  
  jdbcTemplate = new JdbcTemplate(dataSource);
}

  @Test
  public void testData(){
    //if we need to update or insert somde data 
    udbcTemplate.update("Insert into product ... ");
    // test cases using the dao
    productDao.fetchData();
    assertEquals("Test this",expected,actual);
  }
}

```
