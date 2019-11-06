import java.io.*;
import java.util.*;

class NSITRBAC {
    
    static HashMap<String, Role> roleHierarchy = new HashMap();
    
    public static void main(String[] args) throws Exception {
        File rhfp = new File("roleHierarchy.txt");
        BufferedReader br = new BufferedReader(new FileReader(rhfp));

        String str;
        String[] roles = new String[2];
        HashMap<String, Role> newRoles = new HashMap();
        while((str = br.readLine()) != null) {
            roles = str.split("\\s+");
            Role newRole = new Role();
            newRole.roleName = roles[0];
            if (newRoles.containsKey(newRole.roleName) && (!newRoles.get(roles[0]).descendantName.equals(roles[1]))) {
                System.out.println("Invalid role hierarchy. Roles may only have one descendant.");
                System.out.print("Attempting to assign " + roles[0] + " to " + roles[1] + " but ");
                System.out.print("already assigned to " + newRoles.get(roles[0]).descendantName + ".\n");
                System.exit(0);
            }
            newRole.descendantName = roles[1];
            newRoles.put(newRole.roleName, newRole);
            System.out.println(roles[0] + '\t' + roles[1]);
        }

        br.close();

        ArrayList<String> headRole = new ArrayList<String>();

        roleHierarchy.putAll(newRoles);

        // System.out.println("***" + roleHierarchy.get("R4").descendantName);

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

            System.out.println("Role: " + role.getKey() + ",\tDescendant: " + role.getValue().descendantName);
        }

        

        for (int i = 0; i < headRole.size(); i++) {
            System.out.println("\n");
            printHierarchy(headRole.get(i), 0);
        }

    }

    public static void printHierarchy(String role, int level) throws NullPointerException {
        level++;
        System.out.println(role);
        String branch;
        ArrayList<Role> ascendants = roleHierarchy.get(role).ascendant;
        if (!ascendants.isEmpty()) {
            for (int i = 0; i < ascendants.size(); i++) {
                if ((i+1) == ascendants.size()) {
                    branch = "└──";
                } else {
                    branch = "├──";
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
