package xyz.ryansbeanfactory.hashmark.hashmark.service.hashing.struct;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class MerkleTree {

    MerkleNode root;
    List<String> leafHashes;

    public MerkleTree(MerkleNode root, List<String> leafHashes) {
        this.root = root;
        this.leafHashes = leafHashes;
    }

    public String getRootHash() {
        return root.getHash();
    }

    public String printMerkleTree() {

        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("============================================================\n");
        sb.append("                     MERKLE TREE\n");
        sb.append("============================================================\n");

        // Root
        sb.append("\nRoot Hash:\n");
        sb.append("  ").append(root != null ? root.hash : "NULL").append("\n");

        // Leaves
        sb.append("\nLeaf Hashes (Chunks):\n");

        for (int i = 0; i < leafHashes.size(); i++) {
            sb.append(String.format("  [%02d]  %s%n", i, leafHashes.get(i)));
        }

        sb.append("\n============================================================\n");

        return sb.toString();
    }
}