import java.io.*;
import java.util.*;

class NSITRBAC {

    // static variable declarations
    static HashMap<String, Role> roleHierarchy = new HashMap<String, Role>();
    
    // public static HashMap<String, Role> getUsers(String roleHierarchyFile) {

    // }


    public static void main(String[] args) throws Exception {
        System.out.print("\033[H\033[2J");
        File rhfp = new File("roleHierarchy.txt");
        BufferedReader br = new BufferedReader(new FileReader(rhfp));

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

        br.close();

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

        for (int i = 0; i < headRole.size(); i++) {
            System.out.println("\n");
            ArrayList<Integer> branches = new ArrayList<Integer>();
            printHierarchy(headRole.get(i), 0, branches);
        }

        // Needs validation
        File rObj = new File("resourceObjects.txt");
        BufferedReader br2 = new BufferedReader(new FileReader(rObj));
        ArrayList<String> resObjs = new ArrayList<String>(Arrays.asList(br2.readLine().split("\\s+")));

        System.out.println(resObjs);

        Grid accessControlMatrix = new Grid();
        accessControlMatrix.colLabels = resObjs;
        accessControlMatrix.rowLabels = new ArrayList<String>(roleHierarchy.keySet());
        accessControlMatrix.print();

    }

    public static void printHierarchy(String role, int level, ArrayList<Integer> branches) throws NullPointerException {
        level++;
        System.out.println(role);
        String branch;
        ArrayList<Role> ascendants = roleHierarchy.get(role).ascendant;
        if (!ascendants.isEmpty()) {
            for (int i = 0; i < ascendants.size(); i++) {
                if ((i+1) == ascendants.size()) {
                    branch = "└──";
                    branches.remove(Integer.valueOf(level));
                } else {
                    branch = "├──";
                    if (!branches.contains(level)) {
                        branches.add(level);
                    }
                }
                for (int indent = 1; indent < level; indent++) {
                    if (branches.contains(indent
                    )) {
                        System.out.print("\u2502  ");
                    } else {
                        System.out.print("   ");
                    }
                }
                System.out.print(branch);
                String ascendant = roleHierarchy.get(ascendants.get(i).roleName).roleName;
                printHierarchy(ascendant, level, branches);
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
    int nRows;
    int nCols;

    ArrayList<String> rowLabels;
    ArrayList<String> colLabels;

    int maxHeight;

    public void print(){     
        System.out.print("\t");
        for (String col : colLabels) {
            System.out.print(col + "\t");
        }
        System.out.print("\n");
        for (String row : rowLabels) {
            System.out.printf("%4s\n", row);
        }

    }
}
