import hudson.*
import hudson.security.*
import jenkins.model.*
import java.util.*
import com.michelin.cio.hudson.plugins.rolestrategy.*
import com.synopsys.arc.jenkins.plugins.rolestrategy.*
import java.lang.reflect.*
import java.util.logging.*
import groovy.json.*

def env = System.getenv()

/**
 * ===================================
 *         
 *                Roles
 *
 * ===================================
 */
def globalRoleRead = "read"
def globalBuildRole = "build"
def globalRoleAdmin = "admin"
def projectRoleDeveloper = "developer"
/**
 * ===================================
 *         
 *           Users and Groups
 *
 * ===================================
 */
def access = [
  admins: ["anonymous"],
  builders: [],
  readers: [],
  developers: []
]

//def AUTHZ_JSON_FILE = "$WORKSPACE/roles.json"
//def AUTHZ_JSON_FILE = binding.variables.get('AUTHZ_JSON_FILE')

def AUTHZ_JSON_FILE = build.project.getWorkspace().child("roles.json")
// echo $$AUTHZ_JSON_FILE

try { 
  if ( "${AUTHZ_JSON_FILE}" == "" && env.AUTHZ_JSON_FILE != null )
    // If default value is empty and env variable defined, use this env variable
    AUTHZ_JSON_FILE = "${env.AUTHZ_JSON_FILE}" 
} catch(ex) { }
try { 
  if ( "${AUTHZ_JSON_URL}" == "" && env.AUTHZ_JSON_URL != null )
    // If default value is empty and env variable defined, use this env variable
    AUTHZ_JSON_URL  = "${env.AUTHZ_JSON_URL}"  
} catch(ex) { }



if ( "${AUTHZ_JSON_FILE}" != "")  {
  println "Get role authorizations from file ${AUTHZ_JSON_FILE}"
  File f = new File("${AUTHZ_JSON_FILE}")
  def jsonSlurper = new JsonSlurper()
  def jsonText = f.getText()
  access = jsonSlurper.parseText( jsonText )
}
else if ( "${AUTHZ_JSON_URL}" != "") {
  println "Get role authorizations from URL ${AUTHZ_JSON_URL}"
  URL jsonUrl = new URL("${AUTHZ_JSON_URL}");
  access = new JsonSlurper().parse(jsonUrl);
}
else {
  println "Warning! Neither AUTHZ_JSON_FILE nor AUTHZ_JSON_URL specified!"
  println "Granting anonymous admin access"
} 

/**
 * ===================================
 *         
 *           Permissions
 *
 * ===================================
 */

// TODO: drive these from a config file
def adminPermissions = [
"hudson.model.Hudson.Administer",
"hudson.model.Hudson.Read"
]

def readPermissions = [
"hudson.model.Hudson.Read",
"hudson.model.Item.Discover",
"hudson.model.Item.Read"
]

def buildPermissions = [
"hudson.model.Hudson.Read",
"hudson.model.Item.Build",
"hudson.model.Item.Cancel",
"hudson.model.Item.Read",
"hudson.model.Run.Replay"
]

def developerPermissions = [
"hudson.model.Item.Build",
"hudson.model.Item.Cancel",
"hudson.model.Item.Configure",
"hudson.model.Item.Create",
"hudson.model.Item.Delete",
"hudson.model.Item.Discover",
"hudson.model.Item.Move",
"hudson.model.Item.Read",
"hudson.model.Item.Workspace"
]

def roleBasedAuthenticationStrategy = new RoleBasedAuthorizationStrategy()
Jenkins.instance.setAuthorizationStrategy(roleBasedAuthenticationStrategy)


/**
 * ===================================
 *         
 *               HACK
 * Inspired by https://issues.jenkins-ci.org/browse/JENKINS-23709
 * Deprecated by on https://github.com/jenkinsci/role-strategy-plugin/pull/12
 *
 * ===================================
 */

Constructor[] constrs = Role.class.getConstructors();
for (Constructor<?> c : constrs) {
  c.setAccessible(true);
}

// Make the method assignRole accessible
Method assignRoleMethod = RoleBasedAuthorizationStrategy.class.getDeclaredMethod("assignRole", RoleType.class, Role.class, String.class);
assignRoleMethod.setAccessible(true);
println("HACK! changing visibility of RoleBasedAuthorizationStrategy.assignRole")

/**
 * ===================================
 *         
 *           Permissions
 *
 * ===================================
 */

Set<Permission> adminPermissionSet = new HashSet<Permission>();
adminPermissions.each { p ->
  def permission = Permission.fromId(p);
  if (permission != null) {
    adminPermissionSet.add(permission);
  } else {
    println("${p} is not a valid permission ID (ignoring)")
  }
}

Set<Permission> buildPermissionSet = new HashSet<Permission>();
buildPermissions.each { p ->
  def permission = Permission.fromId(p);
  if (permission != null) {
    buildPermissionSet.add(permission);
  } else {
    println("${p} is not a valid permission ID (ignoring)")
  }
}

Set<Permission> readPermissionSet = new HashSet<Permission>();
readPermissions.each { p ->
  def permission = Permission.fromId(p);
  if (permission != null) {
    readPermissionSet.add(permission);
  } else {
    println("${p} is not a valid permission ID (ignoring)")
  }
}

Set<Permission> developerPermissionSet = new HashSet<Permission>();
developerPermissions.each { p ->
  def permission = Permission.fromId(p);
  if (permission != null) {
    developerPermissionSet.add(permission);
  } else {
    println("${p} is not a valid permission ID (ignoring)")
  }
}

/**
 * ===================================
 *         
 *      Permissions -> Roles
 *
 * ===================================
 */

// admins
Role adminRole = new Role(globalRoleAdmin, adminPermissionSet);
roleBasedAuthenticationStrategy.addRole(RoleType.Global, adminRole);

// builders
Role buildersRole = new Role(globalBuildRole, buildPermissionSet);
roleBasedAuthenticationStrategy.addRole(RoleType.Global, buildersRole);

// anonymous read
Role readRole = new Role(globalRoleRead, readPermissionSet);
roleBasedAuthenticationStrategy.addRole(RoleType.Global, readRole);

// developers
Role developerRole = new Role(projectRoleDeveloper, developerPermissionSet);
roleBasedAuthenticationStrategy.addRole(RoleType.Project, developerRole);

/**
 * ===================================
 *         
 *      Roles -> Groups/Users
 *
 * ===================================
 */

access.admins.each { l ->
  println("Granting admin to ${l}")
  roleBasedAuthenticationStrategy.assignRole(RoleType.Global, adminRole, l);  
}

access.builders.each { l ->
  println("Granting builder to ${l}")
  roleBasedAuthenticationStrategy.assignRole(RoleType.Global, buildersRole, l);  
}

access.readers.each { l ->
  println("Granting read to ${l}")
  roleBasedAuthenticationStrategy.assignRole(RoleType.Global, readRole, l);  
}

access.developers.each { l ->
  println("Granting developer to ${l}")
  roleBasedAuthenticationStrategy.assignRole(RoleType.Project, developerRole, l);  
}

Jenkins.instance.save()