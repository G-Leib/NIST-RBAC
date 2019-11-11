import java.io.*;
import java.util.*;


class NSITRBAC {

    static HashMap<String, Role> roleHierarchy = new HashMap<String, Role>();
    static ArrayList<String> resourceObjects = new ArrayList<String>();
    static ArrayList<String> headRole = new ArrayList<String>();
    static Grid roleObjectMatrix = new Grid();
    static Grid userRoleMatrix = new Grid();
    static HashMap<Integer, ArrayList<String>> ssdConstraints = new HashMap<Integer, ArrayList<String>>();
    static HashMap<String, ArrayList<String>> userRoles = new HashMap<String, ArrayList<String>>();


    

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
        roleObjectMatrix.printACM();

        assignBasePermissions();
        getPermissions("permissionsToRoles.txt");

        for (int i = 0; i < headRole.size(); i++) {
            roleHierarchy.get(headRole.get(i)).permissions = inheritPermissions(headRole.get(i));
        }

        roleObjectMatrix.roleHierarchy = roleHierarchy;

        roleObjectMatrix.printACM();

        getSSD("roleSetsSSD.txt");

        getUsers("userRoles.txt");

        userRoleMatrix.users = userRoles;
        userRoleMatrix.rowLabels = new ArrayList<String>(userRoles.keySet());
        userRoleMatrix.colLabels = new ArrayList<String>(roleHierarchy.keySet());
        userRoleMatrix.printURM();

        queryConsole();

    }

    public static void queryConsole() throws IOException {
        Scanner scan = new Scanner(System.in);
        while(true) {
            boolean allObjects = false;
            boolean allRights = false;
            boolean hasRights = false;
            System.out.print("Please enter the user in your query: ");
            String user = scan.nextLine();
            while(!userRoles.containsKey(user)){
                System.out.print("Invalid user, try again: ");
                user = scan.nextLine();
            }
            System.out.println("Please enter the object in your query (hit enter for any): ");
            String object = scan.nextLine();
            while(!resourceObjects.contains(object) & object != null & !object.isEmpty()) {
                System.out.print("Invalid object, try again: ");
                object = scan.nextLine();
            }
            System.out.println("Please enter the access right in your query (hit enter for any): ");
            String accessRight = scan.nextLine();

            if(object == null | object.isEmpty()){
                allObjects = true;
            }
            if(accessRight == null | accessRight.isEmpty()){
                allRights = true;
            }
            
            ArrayList<String> roles = userRoles.get(user);

            if(allObjects & allRights){
                for(int r = 0; r < roles.size(); r++) {
                    for(HashMap.Entry<String, ArrayList<String>> perm : roleHierarchy.get(roles.get(r)).permissions.entrySet()) {
                        for(int o = 0; o < perm.getValue().size(); o++) {
                            if(!perm.getValue().get(o).isEmpty())
                                System.out.println(perm.getKey() + "\t" + perm.getValue());
                        }
                    }
                }
            } else {
                ArrayList<String> rights = new ArrayList<String>();
                for(int r = 0; r < roles.size(); r++) {
                    rights = roleHierarchy.get(roles.get(r)).permissions.get(object);
                    if (!allObjects & allRights) {
                        System.out.println(roles.get(r));
                    } else if(rights.contains(accessRight)) {
                        hasRights = true;
                    }

                }
            }

            if(!allObjects & !allRights) {
                if(hasRights) {
                    System.out.println("Accepted");
                } else {
                    System.out.println("Rejected");
                }
            }

            System.out.print("Would you like to continue for the next query? ");
            String anotherQuery = scan.nextLine();
            if(!anotherQuery.equalsIgnoreCase("yes")) {
                System.out.println("\nGoodbye");
                System.exit(0);
            }


        }
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

    public static void getRoles(String fname) throws IOException, FileNotFoundException{
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

    public static void getResources(String fname) throws IOException, FileNotFoundException{
        

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

    public static void getPermissions(String fname) throws IOException, FileNotFoundException {
        String str = new String();
        // Needs validation
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

    public static HashMap<String, ArrayList<String>> inheritPermissions(String role) throws Exception {
        ArrayList<Role> ascendants = roleHierarchy.get(role).ascendant;
        if (!ascendants.isEmpty()) {
            for (int i = 0; i < ascendants.size(); i++) {
                HashMap<String, ArrayList<String>> ascPerm = inheritPermissions(ascendants.get(i).roleName);
                for(HashMap.Entry<String, ArrayList<String>> perm : ascPerm.entrySet()) {
                    roleHierarchy.get(role).permissions.get(perm.getKey()).addAll(perm.getValue());
                }
            }


        }
        
        return roleHierarchy.get(role).permissions;
        
    }

    public static void getSSD(String fname) throws IOException, FileNotFoundException {
        File ssdf = new File(fname);
        String str;
        boolean valid;
        
        
        do {
            valid = true;
            BufferedReader br = new BufferedReader(new FileReader(ssdf));
            int lineNumber = 1;
            int c = 1;
            while((str = br.readLine()) != null) {
                ArrayList<String> rules = new ArrayList<String>(Arrays.asList(str.split("\\s+")));
                int n = Integer.parseInt(rules.get(0));
                if(n < 2) {
                    br.close();
                    System.out.println("Invalid line found in roleSetsSSD.txt: line " + lineNumber);
                    System.out.println("Press enter to read it again.");
                    try {
                        System.in.read();
                    } catch(Exception e) {}
                    ssdConstraints.clear();
                    valid = false;
                    break;
                }
                ssdConstraints.put(c, rules);
                c++;
                lineNumber++;
            }
            br.close();   

        } while(!valid);

        
        for(HashMap.Entry<Integer, ArrayList<String>> cons : ssdConstraints.entrySet()) {
            List<String> roleSet = cons.getValue().subList(1, cons.getValue().size());
            System.out.println("Constrant " + cons.getKey() + ", n = " + cons.getValue().get(0) + ", set of roles = " + roleSet);

        }
        

    }

    public static void getUsers(String fname) throws IOException, FileNotFoundException {
        File urf = new File(fname);
        String str;
        boolean valid;

        do {
            valid = true;
            userRoles.clear();
            BufferedReader br = new BufferedReader(new FileReader(urf));
            int lineNumber = 1;
            while((str = br.readLine()) != null) {
                ArrayList<String> newUserRoles = new ArrayList<String>(Arrays.asList(str.split("\\s+")));
                ArrayList<String> roles = new ArrayList<String>(newUserRoles.subList(1, newUserRoles.size()));
                String user = newUserRoles.get(0);
                if(userRoles.containsKey(user)) {
                    br.close();
                    System.out.println("Invalid line found at line: " + lineNumber + " due to duplicate user.");
                    System.out.println("Press enter to read again.");
                    valid = false;
                    try {
                        System.in.read();
                    } catch(Exception e) {}
                    break;
                }

                int constraint = -1;
                for(HashMap.Entry<Integer, ArrayList<String>> cons : ssdConstraints.entrySet()) {
                    int c = 0;
                    List<String> roleSet = cons.getValue().subList(1, cons.getValue().size());
                    for(int r = 0; r < roles.size(); r++) {
                        if (roleSet.contains(roles.get(r))) {
                            c ++;
                        }
                    }
                    if(c >= Integer.parseInt(cons.getValue().get(0))) {
                        valid = false;
                        constraint = cons.getKey();
                    }
                }
                if (!valid) {
                    br.close();
                    System.out.println("Invalid line found at line: " + lineNumber + " due to constraint #" + constraint + ".");
                    System.out.println("Press enter to read again.");
                    try {
                        System.in.read();
                    } catch(Exception e) {}
                    break;
                }
                lineNumber++;
                userRoles.put(user, roles);

            }
                

        } while(!valid);

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
    HashMap<String, ArrayList<String>> users = new HashMap<String, ArrayList<String>>();

    ArrayList<String> rowLabels;
    ArrayList<String> colLabels;

    int maxHeight = 1;
    int maxWidth = 4;

    public void printACM(){
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

    public void printURM(){
        maxWidth = 3;
        String lines = "";
        for(int l = 0; l < (maxWidth+2); l++) {
            lines = lines + "\u2501";
        }
        String lastRow = rowLabels.get(rowLabels.size()-1);

        // Top left corner
        System.out.print("\n");
        System.out.print("     \u2502");

        // Column headers
        for (String col : colLabels) {
            System.out.printf(" %3s \u2502", col);
        }

        // Rows print loop
        System.out.print("\n");
        for (String row : rowLabels) {
            // Top edge of row
            for (String col : colLabels) {
                System.out.print(lines + "\u254b");
            }
            System.out.println(lines + "\u254b");

            System.out.printf(" %3s \u2502", row);
            for (String col : colLabels) {
                ArrayList<String> roles = users.get(row);
                if (roles.contains(col)) {
                    System.out.print("  +  \u2502");
                } else {
                    System.out.print("     \u2502");
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
