// Luis Estevez
// Vincent Viloria
// CPE 315-07
// Lab 3

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class lab3 {

   static final Map<String, String[]> R_INSTRS = new HashMap<String, String[]>();
   static final Map<String, String> I_INSTRS = new HashMap<String, String>();
   static final Map<String, String> J_INSTRS = new HashMap<String, String>();
   static final HashMap<String, Integer> REGISTERS = new LinkedHashMap<String, Integer>();

   // TODO: separate everything into different files, getting kinda cluttered

   public static void executeCommand(String command, int[] memoryArr, int pc, List<Instruction> instrArr) {
      char firstChar = command.charAt(0);
      if (firstChar == 'h') {
         System.out.println("h = show help");
         System.out.println("d = dump register state");
         System.out.println("s = single step through the program (i.e. execute 1 instruction and stop");
         System.out.println("s num = step through num instructions of the program");
         System.out.println("r = run until the program ends");
         System.out.println("m num1 num2 = display data memory from location num1 to num2");
         System.out.println("c = clear all registers, memory, and the program counter to 0");
         System.out.println("q = exit the program");
      }
      else if (firstChar == 'd') {
         System.out.println();
         System.out.println("pc = " + Integer.toString(pc));
         registersToString();
      }
      else if (firstChar == 's') {
         System.out.println("command: s");
      }
      else if (command.equals("s num")) {
         System.out.println("command: s");
      }
      else if (firstChar == 'r') {
         System.out.println("command: s");
      }

      else if (firstChar == 'm') {
         String[] splitStr = command.split(" ");
         if(splitStr.length == 3){
            int start = Integer.parseInt(splitStr[1]);
            int end = Integer.parseInt(splitStr[2]);

            if(start > end)
               System.out.println("\nFirst Number can't be Greater than the Second Number\n");

            else if(start < 8192 && end < 8192){
               System.out.println();
               for(int i = start; i <= end; i++){
                  System.out.println("[" + i + "] = " + memoryArr[i]);
               }
               System.out.println();
            }
            else
               System.out.println("\nNumber can't be greater than 8191\n");
         }
         else
            System.out.println("\nERROR: m num1 num2\n");         
      }

      else if (firstChar == 'c') {
        pc = 0;
        for (Map.Entry<String, Integer> entry : REGISTERS.entrySet()) {
            REGISTERS.put(entry.getKey() , 0);
        }
        Arrays.fill(memoryArr, 0);
      }
      else {
         System.out.println("Invalid command");
      }
   }

   public static void executeInstruction(Instruction instr, int PC, Integer[] MEMORY){
      int operator;
      if(instr.getOpcode().equals("add")){
         operator = REGISTERS.get(instr.getRs()) + REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("or")){
         operator = REGISTERS.get(instr.getRs()) | REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("and")){
         operator = REGISTERS.get(instr.getRs()) & REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("sll")){
         operator = REGISTERS.get(instr.getRt()) << REGISTERS.get(instr.getShamt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("sub")){
         operator = REGISTERS.get(instr.getRs()) - REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("slt")){
         if(REGISTERS.get(instr.getRs()) < REGISTERS.get(instr.getRt()))
            REGISTERS.put(instr.getRd(), 1);
         REGISTERS.put(instr.getRd(), 0);
      }
      else if(instr.getOpcode().equals("jr")){
         PC = REGISTERS.get(instr.getRs());
      }
      else if(instr.getOpcode().equals("addi")){
         operator = REGISTERS.get(instr.getRs()) + Integer.parseInt(instr.getImm());
         REGISTERS.put(instr.getRt(), operator);
      }
      else if(instr.getOpcode().equals("beq")){
         if(REGISTERS.get(instr.getRs()) == REGISTERS.get(instr.getRt()))
            PC = PC + 4 + REGISTERS.get(instr.getAddr());
      }
      else if(instr.getOpcode().equals("bne")){
         if(REGISTERS.get(instr.getRs()) != REGISTERS.get(instr.getRt()))
            PC = PC + 4 + REGISTERS.get(instr.getAddr());
      }
      else if(instr.getOpcode().equals("lw")){
         operator = MEMORY[REGISTERS.get(instr.getRs()) + Integer.parseInt(instr.getImm())];
         REGISTERS.put(instr.getRt(), operator);
      }
      else if(instr.getOpcode().equals("sw")){
         operator = REGISTERS.get(instr.getRs()) + Integer.parseInt(instr.getImm());
         MEMORY[operator] = REGISTERS.get(instr.getRt());
      }
      else if(instr.getOpcode().equals("j")){
         PC = Integer.parseInt(instr.getAddr());
      }
      else if(instr.getOpcode().equals("jal")){
         REGISTERS.put("ra", PC + 4);
         PC = Integer.parseInt(instr.getAddr());
      }
      else{
         System.out.println("invalid oppcode");
      }
   }

   public static void registersToString(){
      int counter = 0;
      String format = "%s%-20s";

      for (Map.Entry<String, Integer> entry : REGISTERS.entrySet()) {
         System.out.printf(format, "$", entry.getKey() + " = " + entry.getValue());
         //System.out.print("$" + entry.getKey() + " = " + entry.getValue() + "\t");
         if(counter == 3){
            System.out.println();
            counter = 0;
            continue;
         }  
         counter++;
      }
      
      System.out.println("\n");
   }

   public static Instruction getInstruction(String line, HashMap<String, Integer> labels, int lineNumber) {
      // assumes line DOES NOT start with a label
      Instruction instr;
      String[] splitStr = line.split("[,$ ]+");
      String op = splitStr[0].trim();

      if (R_INSTRS.containsKey(op)) {
         instr = generateRInstruction(splitStr);
      }

      else if (I_INSTRS.containsKey(op)) {
         instr = generateIInstruction(splitStr, labels, lineNumber);
      }

      else if (J_INSTRS.containsKey(op)) {
         instr = generateJInstruction(splitStr, labels);
      }

      else {
         return null;
      }

      return instr;
   }

   public static String addLeadingZeros(String binString, int numBits) {
      while (binString.length() < numBits) {
         binString = "0" + binString;
      }
      return binString;
   }

   public static String getShamt(String decimalStr) {
      int shamtInt = Integer.parseInt(decimalStr);
      String shamt = Integer.toBinaryString(shamtInt);
      shamt = addLeadingZeros(shamt, 5);
      return(" " + shamt);
   }

   public static Instruction generateRInstruction(String[] instr) {
      // needs to return instruction class, but for now string
      String rd=" 00000", rs=" 00000", rt=" 00000",
         shamt=" 00000", funct=" 000000";
      String oper = instr[0].trim(); 
      String opcode = R_INSTRS.get(oper)[0];

      if (oper.equals("sll")) {
         shamt = getShamt(instr[3]);
         rt = instr[2];
         rd = instr[1];
         funct = R_INSTRS.get(oper)[1];
      }
      else if (oper.equals("jr")) {
         rs = instr[1];
         funct = R_INSTRS.get(oper)[1];
      }
      else {
         rs = instr[2];
         rt = instr[3];
         rd = instr[1];
         funct = R_INSTRS.get(oper)[1];
      }
      return(new Instruction(oper, rs, rt, rd, shamt, funct, "", ""));
   }

   public static Instruction  generateIInstruction(String[] instr, HashMap<String, Integer> labels, int lineNumber) {
      String rs="", rt="", imm ="", immWithPadding="";
      String op = instr[0].trim(); 
      String opcode = I_INSTRS.get(op);

      if (op.equals("bne") || op.equals("beq")){
         String label = instr[3].replaceAll("\\s+", "");
         int branchNum = labels.get(label) - (lineNumber+1);

         rs = instr[1].replaceAll("[^a-z0-9]","");
         rt = instr[2].replaceAll("[^a-z0-9]","");
         return(new Instruction(op, rs, rt, "", "", "", "", String.valueOf(branchNum)));
      }

      else if(op.equals("lw") || op.equals("sw")){
         String value = instr[2].replaceAll("[^0-9]","");
         rt = instr[3].replaceAll("[^a-z0-9]","");
         rs = instr[1].replaceAll("[^a-z0-9]","");
         return(new Instruction(op, rs, rt, "", "", "", value, ""));
      }

      else{
         rt = instr[2].replaceAll("[^a-z0-9]","");
      }

      rs = instr[1].replaceAll("[^a-z0-9]","");
      return(new Instruction(op, rs, rt, "", "", "", instr[3], ""));
   }

   public static  Instruction generateJInstruction(String[] instr, HashMap<String, Integer> labels) {
      String addr ="", addrWithPadding = "";
      String label = instr[1].replaceAll("\\s+", "");
      String opcode = instr[0];
   
      return new Instruction(opcode, "", "", "", "", "", "", Integer.toString(labels.get(label)));
   }

   public static String removeComments(String line) {
      if (line.indexOf("#") > -1)
         return line.substring(0, line.indexOf("#"));
      return line;
   }

   public static void labelFinder(String line, HashMap<String, Integer> labels, int lineNumber){
      String[] arr = line.split(":");
      if(line.indexOf(":") != -1) {
        labels.put(arr[0], lineNumber);
      }
   }

   public static HashMap<String, Integer> getLabels(File inputFile) {
      HashMap<String, Integer> labels = new HashMap<String, Integer>();
      int line = 0;
      try {
         Scanner scanner = new Scanner(inputFile); 
         while (scanner.hasNextLine()) {
            String lineStr = scanner.nextLine(); // each line is a string
            labelFinder(lineStr, labels, line);

            String trimmedLine = lineStr.replaceFirst("^\\s+", "");
            if((trimmedLine.isEmpty()) || (trimmedLine.startsWith("#"))){
              continue;
            }

            line++;

         }
         scanner.close();
      }
      catch (FileNotFoundException e) {
         System.out.println("Oh no my code");
         e.printStackTrace();
      }
      return labels;
   }

   public static  List<Instruction> createInstructions(File inputFile, HashMap<String, Integer> labels) {
      List<Instruction> instrArr = new ArrayList<Instruction>();
      Instruction instr;
      int line = 0;
      try {
         Scanner scanner = new Scanner(inputFile);
         while (scanner.hasNextLine()) {
            String lineStr = removeComments(scanner.nextLine().trim());
            String[] lineArr = lineStr.split(":");
            // line has a label followed by instr
            if (lineArr.length == 2) {
               instr = getInstruction(lineArr[1].trim(), labels, line);
               line++;
            }
            // line does not have a label and is not whitespace or empty str
            else if (lineArr.length == 1 && lineStr.indexOf(":") == -1 &&
               lineStr.length() > 0) {
               instr = getInstruction(lineStr.trim(),labels, line);
               line++;
            }
            else { continue; }

            if(instr == null){
               String[] splitStr = lineStr.split("[,$ ]+");
               String op = splitStr[0].trim();
               instrArr.add(null);
               return instrArr;
            }
            instrArr.add(instr);
         }
         scanner.close();
      }
      catch (FileNotFoundException e) {
         System.out.println("Oh no my code");
         e.printStackTrace();
      }
      return instrArr;
   }

   public static void main(String[] args) {
      int[] memoryArr = new int[8192];
      int pc = 0;
      List<Instruction> instrArr;
      String filename = args[0];
      Scanner scanner = new Scanner(System.in);
      String command;

      File inputFile = new File(filename);
      HashMap<String, Integer> labels = getLabels(inputFile);
      instrArr = createInstructions(inputFile, labels);
      // args: filename script
      if (args.length == 1) {
         // run interactive mode
         while (true) {
            System.out.print("mips> ");
            command = scanner.nextLine();
            if (command.trim().equals("q"))
               break;
            executeCommand(command, memoryArr, pc, instrArr);
         }
      }
      else {
         // run script mode
         System.out.println("Running script mode");
      }
   }

   static {
      // instructions are stored as key=operation, val=[opcode, funct]
    R_INSTRS.put("add", new String[] {"000000", " 100000"});
    R_INSTRS.put("or", new String[]  {"000000", " 100101"});
    R_INSTRS.put("and", new String[] {"000000", " 100100"});
    R_INSTRS.put("sll", new String[] {"000000", " 000000"});
    R_INSTRS.put("sub", new String[] {"000000", " 100010"});
    R_INSTRS.put("slt", new String[] {"000000", " 101010"});
    R_INSTRS.put("jr", new String[]  {"000000", " 001000"});
    
    I_INSTRS.put("addi", "001000");
    I_INSTRS.put("beq", "000100");
    I_INSTRS.put("bne", "000101");
    I_INSTRS.put("lw", "100011");  
    I_INSTRS.put("sw", "101011");

    J_INSTRS.put("j", "000010");
    J_INSTRS.put("jal", "000011");
 
    //REGISTERS.put("zero", 0);
    REGISTERS.put("0",  0);
    REGISTERS.put("v0", 0);
    REGISTERS.put("v1", 0);
    REGISTERS.put("a0", 0);
    REGISTERS.put("a1", 0);
    REGISTERS.put("a2", 0);
    REGISTERS.put("a3", 0);
    REGISTERS.put("t0", 0);
    REGISTERS.put("t1", 0);
    REGISTERS.put("t2", 0);
    REGISTERS.put("t3", 0);
    REGISTERS.put("t4", 0);
    REGISTERS.put("t5", 0);
    REGISTERS.put("t6", 0);
    REGISTERS.put("t7", 0);
    REGISTERS.put("s0", 0);
    REGISTERS.put("s1", 0);
    REGISTERS.put("s2", 0);
    REGISTERS.put("s3", 0);
    REGISTERS.put("s4", 0);
    REGISTERS.put("s5", 0);
    REGISTERS.put("s6", 0);
    REGISTERS.put("s7", 0);
    REGISTERS.put("t8", 0);
    REGISTERS.put("t9", 0);
    REGISTERS.put("sp", 0);
    REGISTERS.put("ra", 0);
   }

}
