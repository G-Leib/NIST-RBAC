import java.io.*;
import java.util.*;


class NSITRBAC {
    // TODO:
    // input loop
    // 2.2 validation
    // 2.3 outer branches (visual fix)
    // 3 read object file
    // 3.1 validate object file
    // 3.2 print empty matrix
    // 4.5 read permissionsToroles.txt
    // 4.1a grant permissions
    // 4.2 ignore redundant permissions
    // 4.3 each role controls itself
    // 4.4 each role owns ascendants
    // 4.1b inherit roles
    // 4.6 display new ACM
    // 5 read roleSetsSSD.txt
    // 5.1 validate n
    // 5.2 Display constraints
    // 6 read userRoles.txt
    // 6.1a validate repeat users
    // 6.1b validate SSD constraint violations
    // 6.2 display user-role matrix
    // 7 query users
    // 7.1 display prompts for queries (allow empty for access rights and objects)
    // 7.2 invalid user if user not found back to 7.1
    // 7.3 if accessRights and/or object selections empty, display all accessRights and/or objects go to 7.7
    // 7.4 invalid user if object not found back to 7.1
    // 7.5 if accessright empty display all rights on this object go to 7.7
    // 7.6 check if user has right to object "authorized" if yes "rejected" otherwise
    // 7.7 "another query?"


    static HashMap<String, Role> roleHierarchy = new HashMap<String, Role>();
    

    public static void main(String[] args) throws Exception {

        String os = System.getProperty("os.name");
        if(os.contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            Runtime.getRuntime().exec("clear");
        }

        ArrayList<String> headRole = getRoles("roleHierarchy.txt");
              
        for (int i = 0; i < headRole.size(); i++) {
            System.out.println("\n");
            printHierarchy(headRole.get(i), 0);
        }

        // 3 Get Objects

        //3.2 print matrix

    }

    public static ArrayList<String> getRoles(String roleHierarchyFile) throws Exception {
        File f = new File(roleHierarchyFile);
        BufferedReader br = new BufferedReader(new FileReader(f));

        // REDO TO COMPLY WITH 2.2

        String str;
        String[] roles = new String[2];
        HashMap<String, Role> newRoles = new HashMap<String, Role>();
        while((str = br.readLine()) != null) {
            roles = str.split("\\s+");
            Role newRole = new Role();
            newRole.roleName = roles[0];
            if (newRoles.containsKey(newRole.roleName) && (!newRoles.get(roles[0]).descendantName.equals(roles[1]))) {
                System.out.println("Invalid role hierarchy. Roles may only have one descendant.");
                System.out.print("Attempting to assign " + roles[0] + " to " + roles[1] + " but ");
                System.out.print("already assigned to " + newRoles.get(roles[0]).descendantName + ".\n");
                br.close();
                System.exit(0);
            }
            newRole.descendantName = roles[1];
            newRoles.put(newRole.roleName, newRole);
        }

        
        ArrayList<String> headRole = new ArrayList<String>();

        roleHierarchy.putAll(newRoles);

        for (HashMap.Entry<String, Role> role : newRoles.entrySet()) {
            String desc = role.getValue().descendantName;
            if (!roleHierarchy.containsKey(desc)) {
                Role newRole = new Role();
                newRole.roleName = desc;
                roleHierarchy.put(desc, newRole);
                headRole.add(desc);
            }
            role.getValue().descendant = roleHierarchy.get(desc);
            roleHierarchy.get(desc).ascendant.add(role.getValue());
        }

        br.close();

        return headRole;
    }


    public static void printHierarchy(String role, int level) throws NullPointerException {
        level++;
        System.out.println(role);
        String branch;
        ArrayList<Role> ascendants = roleHierarchy.get(role).ascendant;
        if (!ascendants.isEmpty()) {
            for (int i = 0; i < ascendants.size(); i++) {
                if ((i+1) == ascendants.size()) {
                    branch = "\u2514\u2501\u2501";
                } else {
                    branch = "\u2523\u2501\u2501";
                }
                if (level > 1) {
                    String indent = String.format("%" + ((level-1)*3) + "s", "");
                    System.out.print(indent + branch);
                } else {
                    System.out.print(branch);
                }
                String ascendant = roleHierarchy.get(ascendants.get(i).roleName).roleName;
                printHierarchy(ascendant, level);
            }
        }
    }
}




class Role {
    Role descendant;
    ArrayList<Role> ascendant = new ArrayList<Role>();

    String roleName;
    String descendantName;
}

class Grid {
    int rows;
    int cols;

    int maxHeight;
    String matrixName;

    public static void printGrid(){ 

    }
}
