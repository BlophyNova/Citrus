package net.blophy.forum.enums

enum class Partitions(val id: Int) {
    BLOPHY(0),
    RELEASE(1),
    TIPS(2),
    OTHER_GAMES(3),
    TECHNOLOGY(4),
    PROGRAMMING(5),
    OTHER(6);

    companion object {
        private val map = entries.associateBy(Partitions::id)
        fun fromId(typeId: Int) = Partitions.map[typeId] ?: OTHER
    }
}