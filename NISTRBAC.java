import java.io.*;
import java.util.*;


class NSITRBAC {
    // TODO:
    // input loop
    // 2.2 validation
    // 2.3 outer branches (visual fix)
    // 3.1 validate object file
    // 4.5 read permissionsToRoles.txt
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

        // clearTerminal();
        
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



        
        ArrayList<String> headRole = new ArrayList<String>();

        // Needs validation
        File rObj = new File("resourceObjects.txt");
        BufferedReader br2 = new BufferedReader(new FileReader(rObj));
        ArrayList<String> newObjects = new ArrayList<String>(Arrays.asList(br2.readLine().split("\\s+")));
        br2.close();
        ArrayList<String> resourceObjects = new ArrayList<String>();

        resourceObjects.addAll(newRoles.keySet());
        resourceObjects.addAll(newObjects);

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
            for (int r = 0; r < resourceObjects.size(); r++) {
                roleHierarchy.get(role.getKey()).permissions.put(resourceObjects.get(r), new ArrayList<String>());
            }
        }

        for (int i = 0; i < headRole.size(); i++) {
            System.out.println("\n");
            ArrayList<Integer> branches = new ArrayList<Integer>();
            printHierarchy(headRole.get(i), 0, branches);
            for (int r = 0; r < resourceObjects.size(); r++) {
                roleHierarchy.get(headRole.get(i)).permissions.put(resourceObjects.get(r), new ArrayList<String>());
            }
        }

        Grid roleObjectMatrix = new Grid();

        roleObjectMatrix.colLabels = resourceObjects;
        roleObjectMatrix.rowLabels = new ArrayList<String>(roleHierarchy.keySet());
        //roleObjectMatrix.print();

        // Add validation
        File ptrf = new File("permissionsToRoles.txt");
        BufferedReader br3 = new BufferedReader(new FileReader(ptrf));
        while((str = br3.readLine()) != null) {
            String[] newPermissions = str.split("\\s+");
            if(!roleHierarchy.get(newPermissions[0]).permissions.containsKey(newPermissions[2])) {
                roleHierarchy.get(newPermissions[0]).permissions.put(newPermissions[2], new ArrayList<String>());
            }
            if(!roleHierarchy.get(newPermissions[0]).permissions.get(newPermissions[2]).contains(newPermissions[1])) {
                roleHierarchy.get(newPermissions[0]).permissions.get(newPermissions[2]).add(newPermissions[1]);
            }
            int width = newPermissions[1].length();
            int height = roleHierarchy.get(newPermissions[0]).permissions.get(newPermissions[2]).size();
            if (width > roleObjectMatrix.maxWidth) {
                roleObjectMatrix.maxWidth = width;
            }
            if (height > roleObjectMatrix.maxHeight) {
                roleObjectMatrix.maxHeight = height;
            }

            
        }
        br3.close();

        roleObjectMatrix.roleHierarchy = roleHierarchy;

        roleObjectMatrix.print();

        System.out.println(roleHierarchy.get("R1").permissions);
        System.out.println(roleHierarchy.get("R2").permissions);
        System.out.println(roleHierarchy.get("R3").permissions);

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

    public static void clearTerminal() throws Exception {
        String os = System.getProperty("os.name");
        if(os.contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            Runtime.getRuntime().exec("clear");
        }

        System.out.print("\033[H\033[2J");
    }

}




class Role {
    Role descendant;
    ArrayList<Role> ascendant = new ArrayList<Role>();

    String roleName;
    String descendantName;

    HashMap<String, ArrayList<String>> permissions = new HashMap<String, ArrayList<String>>();
}

class Grid {
    int nRows;
    int nCols;

    HashMap<String, Role> roleHierarchy = new HashMap<String, Role>();

    ArrayList<String> rowLabels;
    ArrayList<String> colLabels;

    int maxHeight = 1;
    int maxWidth = 4;

    public void print(){
        String lines = "";
        for(int l = 0; l < (maxWidth+2); l++) {
            lines = lines + "\u2501";
        }
        String permission = " ";
        int padAmount;
        int midRow = maxHeight / 2;
        int colHeadSpace = (maxWidth - 1) / 2;
        String lastRow = rowLabels.get(rowLabels.size()-1);

        // Top left corner
        System.out.print("\n");
        System.out.print("\t\u2502");

        // Column headers
        for (String col : colLabels) {
            System.out.printf("%" + colHeadSpace + "s%4s%" + colHeadSpace + "s\u2502", " ", col, " ");
        }

        // Rows print loop
        System.out.print("\n");
        for (String row : rowLabels) {
            // TODO: Fix edges
            // Top edge of row
            for (String col : colLabels) {
                System.out.print(lines + "\u254b");
            }
            System.out.println(lines + "\u254b");

            for(int rowLine = 0; rowLine < maxHeight; rowLine++) {
                
                if(rowLine==midRow) {
                    System.out.printf(" %5s  \u2502", row);
                } else {
                    System.out.printf("%8s\u2502", " ");
                }
                for (String col : colLabels) {

                    if ((!roleHierarchy.get(row).permissions.get(col).isEmpty()) & (rowLine < roleHierarchy.get(row).permissions.get(col).size())) {
                        permission = roleHierarchy.get(row).permissions.get(col).get(rowLine);
                    } else {
                        permission = "  ";
                    }
                    padAmount = (maxWidth + - permission.length()) / 2 + 1;
                    if ((2*padAmount + permission.length()) < (maxWidth+2)) {
                        System.out.print(" ");
                    }
                    System.out.printf("%" + padAmount + "s" + permission + "%" + padAmount + "s\u2502", " ", " ");
                }
                if(rowLine+1<maxHeight) {
                    System.out.print("\n");
                }
            }
            

            if (row.equals(lastRow)) {
                System.out.println();
                for (String col : colLabels) {
                        System.out.print(lines + "\u253b");
                }
                System.out.println(lines + "\u251b");
            }
            System.out.print("\n");
        }

    }
}
