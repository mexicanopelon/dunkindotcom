###!/usr/bin/env groovy
import groovy.json.JsonSlurper


["rm", "-Rf", "/tmp/dunkindotcom"].execute()
["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git", "/tmp/dunkindotcom"].execute()

def commitChanges = ['git, 'show', '--pretty=""', '--name-only'].execute().text
println commitChanges

return commitChanges.contains("commitCheck.groovy")


