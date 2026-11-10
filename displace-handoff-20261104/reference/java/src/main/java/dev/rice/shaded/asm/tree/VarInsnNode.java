package dev.rice.shaded.asm.tree;

import dev.rice.shaded.asm.MethodVisitor;
import java.util.Map;

public class VarInsnNode extends AbstractInsnNode {
   public int var;

   public VarInsnNode(int opcode, int varIndex) {
      super(opcode);
      this.var = varIndex;
   }

   public void setOpcode(int opcode) {
      this.opcode = opcode;
   }

   @Override
   public int getType() {
      return 2;
   }

   @Override
   public void accept(MethodVisitor methodVisitor) {
      methodVisitor.visitVarInsn(this.opcode, this.var);
      this.acceptAnnotations(methodVisitor);
   }

   @Override
   public AbstractInsnNode clone(Map<LabelNode, LabelNode> clonedLabels) {
      return new VarInsnNode(this.opcode, this.var).cloneAnnotations(this);
   }
}
