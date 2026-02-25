package xyz.ryansbeanfactory.hashmark.hashmark.service.hashing.struct;

import java.util.List;

public class MerkleTree {

    MerkleNode root;
    List<String> leafHashes;

    public MerkleTree(MerkleNode root, List<String> leafHashes) {
        this.root = root;
        this.leafHashes = leafHashes;
    }

    public String printMerkleTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("MERKLE TREE\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("\nLeaf Hashes (Chunks): \n");
        for (int i = 0; i < leafHashes.size(); i++) {
            sb.append(" Chunk ").append(i).append(leafHashes.get(i)).append("\n");
        }
        sb.append("\n").append("Root Hash: ").append(root.hash).append("\n");
        sb.append("=".repeat(60)).append("\n");
        return sb.toString();
    }
}
