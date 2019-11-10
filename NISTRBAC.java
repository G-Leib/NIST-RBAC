import java.io.*;
import java.util.*;


class NSITRBAC {
    // TODO:
    // query input loop
    // 4.1b inherit roles
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
    static ArrayList<String> resourceObjects = new ArrayList<String>();
    static ArrayList<String> headRole = new ArrayList<String>();
    static Grid roleObjectMatrix = new Grid();
    static Grid userRoleMatrix = new Grid();


    

    public static void main(String[] args) throws Exception {

        clearTerminal();
        
        getRoles("roleHierarchy.txt");

        for (int i = 0; i < headRole.size(); i++) {
            System.out.println("\n");
            ArrayList<Integer> branches = new ArrayList<Integer>();
            printHierarchy(headRole.get(i), 0, branches);

        }

        getResources("resourceObjects.txt");

        declarePermissions();

        roleObjectMatrix.roleHierarchy = roleHierarchy;


        roleObjectMatrix.colLabels = resourceObjects;
        roleObjectMatrix.rowLabels = new ArrayList<String>(roleHierarchy.keySet());
        roleObjectMatrix.print();

        assignBasePermissions();
        getPermissions("permissionsToRoles.txt");
        // inheritPermissions

        roleObjectMatrix.roleHierarchy = roleHierarchy;

        //roleObjectMatrix.print();

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

    public static void getRoles(String fname) throws IOException{
        File rhfp = new File(fname);

        String str;
        String[] roles = new String[2];
        HashMap<String, Role> newRoles = new HashMap<String, Role>();
        do {
            BufferedReader br = new BufferedReader(new FileReader(rhfp));
            int lineNumber = 1;
            while((str = br.readLine()) != null) {
                roles = str.split("\\s+");
                Role newRole = new Role();
                newRole.roleName = roles[0];
                if (newRoles.containsKey(newRole.roleName) && (!newRoles.get(roles[0]).descendantName.equals(roles[1]))) {
                    br.close();
                    System.out.println("Invalid line is found in roleHierarchy.txt: " + lineNumber + ".");
                    System.out.println("Press enter to read it again.");
                    try {
                        System.in.read();
                    } catch(Exception e) {}
                    break;
                }
                newRole.descendantName = roles[1];
                newRoles.put(newRole.roleName, newRole);

                roleHierarchy.putAll(newRoles);
                lineNumber++;
            }
            if(str == null) {
                br.close();
                break;
            }
        } while(true);


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
    } 

    public static void getResources(String fname) throws IOException{
        

        // Needs validation
        File rObj = new File(fname);
        boolean valid = false;
        ArrayList<String> validResources = new ArrayList<String>();

        do {
            BufferedReader br = new BufferedReader(new FileReader(rObj));
            ArrayList<String> newObjects = new ArrayList<String>(Arrays.asList(br.readLine().split("\\s+")));
            
            br.close();
            for(int i = 0; i < newObjects.size(); i++){
                if (validResources.contains(newObjects.get(i))) {
                    System.out.println("Duplicate resource found: " + newObjects.get(i));
                    System.out.println("Press enter to read it again.");
                    try {
                        System.in.read();
                    } catch(Exception e) {}
                    valid = false;
                    validResources.clear();
                    break;
                } else {
                    validResources.add(newObjects.get(i));
                    valid = true;
                }
            }
        } while(!valid);

        resourceObjects.addAll(roleHierarchy.keySet());
        resourceObjects.addAll(validResources);

    }

    public static void getPermissions(String fname) throws IOException {
        String str = new String();
        File ptrf = new File(fname);
        BufferedReader br = new BufferedReader(new FileReader(ptrf));
        while((str = br.readLine()) != null) {
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
        br.close();
    }

    public static void declarePermissions() throws Exception {
        for (HashMap.Entry<String, Role> role : roleHierarchy.entrySet()) {
            for (int r = 0; r < resourceObjects.size(); r++) {
                roleHierarchy.get(role.getKey()).permissions.put(resourceObjects.get(r), new ArrayList<String>());
            }
        }

    }

    public static void assignBasePermissions() throws Exception {
        // Control self and own ascendants
        for (HashMap.Entry<String, Role> role : roleHierarchy.entrySet()) {

            ArrayList<Role> asc = role.getValue().ascendant;
            if (!asc.isEmpty()) {
                for(int a = 0; a < asc.size(); a++) {
                    role.getValue().permissions.get(asc.get(a).roleName).add("own");
                }
            }
            role.getValue().permissions.get(role.getValue().roleName).add("control");
        }
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
        maxWidth = maxWidth + 2;
        if(maxWidth%2!=0) {
            maxWidth++;
        }
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
        System.out.printf("%" + colHeadSpace + "s%4s%" + colHeadSpace + "s\u2502", " ", " ", " ");

        // Column headers
        for (String col : colLabels) {
            System.out.printf("%" + colHeadSpace + "s%4s%" + colHeadSpace + "s\u2502", " ", col, " ");
        }

        // Rows print loop
        System.out.print("\n");
        for (String row : rowLabels) {
            // Top edge of row
            for (String col : colLabels) {
                System.out.print(lines + "\u254b");
            }
            System.out.println(lines + "\u254b");

            for(int rowLine = 0; rowLine < maxHeight; rowLine++) {
                
                if(rowLine==midRow) {
                    System.out.printf("%" + colHeadSpace + "s%4s%" + colHeadSpace + "s\u2502", " ", row, " ");
                } else {
                    System.out.printf("%" + (maxWidth+2)+ "s\u2502", " ");
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
