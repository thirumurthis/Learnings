package com.demo.wrapper

import spock.lang.Specification

class AppGroovySpockTest extends Specification {

    def "getMessage from app instance"() {
        setup: "a new App instance is created"
        def app = new App()
        when: "getMessage"
        def result = app.getMessage()
        then: "result is as expected"
        result == "app message"
        println "result = ${result}"
    }
}
