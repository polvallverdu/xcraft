package dev.polv.xcraft.forge.ancient.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
//? if >=1.6 {
import net.minecraft.launchwrapper.IClassTransformer;
//?} else {
/*import cpw.mods.fml.relauncher.IClassTransformer;
*///?}

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes Block.harvestBlock(world, player, x, y, z, meta) call OreHarvest.begin(this, player, x, y, z) first and
 * OreHarvest.end() before it returns. The names come from HarvestTemplate's bytecode, which the build remapped to
 * each version's runtime names, so nothing here is version-specific. Must not load any Minecraft class.
 */
public class OreTransformer implements IClassTransformer {
	private final MethodInsnNode harvest, begin, end;

	public OreTransformer() {
		List<MethodInsnNode> calls = templateCalls();
		harvest = calls.get(0);
		begin = calls.get(1);
		end = calls.get(2);
	}

	// Runs before FML's 1.6 deobfuscation transformer (default sorting index), so the class arrives with the
	// obfuscated names the template carries
	//? if >=1.5 {
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		return patch(name, bytes);
	}
	//?} else {
	/*@Override
	public byte[] transform(String name, byte[] bytes) {
		return patch(name, bytes);
	}
	*///?}

	private byte[] patch(String name, byte[] bytes) {
		if (bytes == null || !name.replace('.', '/').equals(harvest.owner)) return bytes;
		ClassNode node = new ClassNode();
		new ClassReader(bytes).accept(node, 0);
		boolean patched = false;
		for (Object m : node.methods) {
			MethodNode method = (MethodNode) m;
			if (!method.name.equals(harvest.name) || !method.desc.equals(harvest.desc)) continue;
			InsnList head = new InsnList();
			head.add(new VarInsnNode(Opcodes.ALOAD, 0));
			head.add(new VarInsnNode(Opcodes.ALOAD, 2));
			head.add(new VarInsnNode(Opcodes.ILOAD, 3));
			head.add(new VarInsnNode(Opcodes.ILOAD, 4));
			head.add(new VarInsnNode(Opcodes.ILOAD, 5));
			head.add(new MethodInsnNode(Opcodes.INVOKESTATIC, begin.owner, begin.name, begin.desc));
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				if (insn.getOpcode() == Opcodes.RETURN) {
					method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, end.owner, end.name, end.desc));
				}
			}
			method.instructions.insert(head);
			patched = true;
		}
		if (!patched) {
			System.err.println("[xCraft] " + harvest.owner + "." + harvest.name + harvest.desc + " not found: ore drops stay vanilla");
			return bytes;
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		System.setProperty("xcraft.oreHook", "true");
		return writer.toByteArray();
	}

	/** The three calls in HarvestTemplate.template, as the build remapped them. */
	private static List<MethodInsnNode> templateCalls() {
		String path = OreTransformer.class.getName().replace('.', '/').replace("OreTransformer", "HarvestTemplate") + ".class";
		InputStream in = OreTransformer.class.getClassLoader().getResourceAsStream(path);
		if (in == null) throw new IllegalStateException("xCraft: " + path + " missing");
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			for (int n; (n = in.read(buffer)) > 0; ) out.write(buffer, 0, n);
			ClassNode node = new ClassNode();
			new ClassReader(out.toByteArray()).accept(node, 0);
			List<MethodInsnNode> calls = new ArrayList<MethodInsnNode>();
			for (Object m : node.methods) {
				MethodNode method = (MethodNode) m;
				if (!method.name.equals("template")) continue;
				for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
					if (insn instanceof MethodInsnNode) calls.add((MethodInsnNode) insn);
				}
			}
			if (calls.size() != 3) throw new IllegalStateException("xCraft: unexpected HarvestTemplate " + calls.size());
			return calls;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				in.close();
			} catch (IOException ignored) {
			}
		}
	}
}
