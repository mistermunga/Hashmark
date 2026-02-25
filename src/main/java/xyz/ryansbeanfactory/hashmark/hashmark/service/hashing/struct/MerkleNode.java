package xyz.ryansbeanfactory.hashmark.hashmark.service.hashing.struct;

public class MerkleNode {

    String hash;
    MerkleNode left;
    MerkleNode right;
    int chunkIndex;

    public MerkleNode(String hash) {
        this.hash = hash;
        this.chunkIndex = -1;
    }

    public MerkleNode(String hash, int chunkIndex) {
        this.hash = hash;
        this.chunkIndex = chunkIndex;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

}
