
public class  Instruction {

   private String opcode, rs, rt, rd, shamt, funct, imm, addr; 

   public Instruction(String opcode, String rs, String rt, String rd,
      String shamt, String funct, String imm, String addr) {
         this.opcode = opcode;
         this.rs = rs;
         this.rt = rt;
         this.rd = rd;
         this.shamt = shamt;
         this.funct = funct;
         this.imm = imm;
         this.addr = addr;
   }

   public String toString() {
      return(opcode + " " + rs + " " + rt + " " + rd + " "
       + shamt + " " + funct + " " + imm + " " + addr);
   }

   public String getOpcode() { return this.opcode; }
   public String getRs() { return this.rs; }
   public String getRt() { return this.rt; }
   public String getRd() { return this.rd; }
   public String getShamt() { return this.shamt; }
   public String getFunct() { return this.funct; }
   public String getImm() { return this.imm; }
   public String getAddr() { return this.addr; }
}

