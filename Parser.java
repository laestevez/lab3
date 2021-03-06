/* 
 * Luis Estevez
 * Vincent Viloria
 * CPE 315-07
 * Lab 3
*/

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.io.PrintStream;

public class Parser {

   static final Map<String, String[]> R_INSTRS = new HashMap<String, String[]>();
   static final Map<String, String> I_INSTRS = new HashMap<String, String>();
   static final Map<String, String> J_INSTRS = new HashMap<String, String>();

   public static String addLeadingZeros(String binString, int numBits) {
      while (binString.length() < numBits) {
         binString = "0" + binString;
      }
      return binString;
   }

   public static String removeComments(String line) {
      if (line.indexOf("#") > -1)
         return line.substring(0, line.indexOf("#"));
      return line;
   }

   public static String getShamt(String decimalStr) {
      int shamtInt = Integer.parseInt(decimalStr);
      String shamt = Integer.toBinaryString(shamtInt);
      shamt = addLeadingZeros(shamt, 5);
      return(" " + shamt);
   }
   public static Instruction getInstruction(String line, HashMap<String, Integer> labels, int lineNumber) {
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
   public static Instruction generateRInstruction(String[] instr) {
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

   public static  List<Instruction> createInstructions(File inputFile, HashMap<String, Integer> labels) {
      List<Instruction> instrArr = new ArrayList<Instruction>();
      Instruction instr;
      int line = 0;
      try {
         Scanner scanner = new Scanner(inputFile);
         while (scanner.hasNextLine()) {
            String lineStr = removeComments(scanner.nextLine().trim());
            String[] lineArr = lineStr.split(":");
            if (lineArr.length == 2) {
               instr = getInstruction(lineArr[1].trim(), labels, line);
               line++;
            }
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
 
   }
}
