package com.tgt.backpackelasticsearch.api

import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants
import com.tgt.swagger_sync.ApiSpec
import com.tgt.swagger_sync.OpenApi3ExtParser
import com.tgt.swagger_sync.SpecComparator
import com.tgt.swagger_sync.SpecVersionComparator
import spock.lang.Specification

class ApiSpecTest extends Specification {

    static String staticSpecRelativePath = "api-specs/backpack-elasticsearch-v1.yml"
    static String dynamicSpecRelativePath = "/build/tmp/kapt3/classes/main/META-INF/swagger/backpack-elasticsearch-v1.yml"

    OpenApi3ExtParser staticSpecFileParser
    OpenApi3ExtParser dynamicSpecFileParser

    def setup() {
        def appDir = System.getProperty("user.dir")

        String staticSpecFilePath = "${appDir}/${staticSpecRelativePath}"
        staticSpecFileParser = new OpenApi3ExtParser(staticSpecFilePath, BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH)

        String dynamicSpecFilePath = "${appDir}${dynamicSpecRelativePath}"
        dynamicSpecFileParser = new OpenApi3ExtParser(dynamicSpecFilePath)
    }

    def "validate dynamically generated swagger spec against static spec for backpack-elasticsearch"() {
        given:
        ApiSpec staticSpec = staticSpecFileParser.parse()
        ApiSpec dynamicSpec = dynamicSpecFileParser.parse()

        when:
        SpecComparator specComparator = new SpecComparator(staticSpec, dynamicSpec)

        then:
        specComparator.match()
    }

    def "test spec version comparison with git master"() {
        given:
        SpecVersionComparator specVersionComparator = new SpecVersionComparator("backpack-elasticsearch-app", staticSpecRelativePath)

        when:
        def result = specVersionComparator.match()

        then:
        result
    }
}
