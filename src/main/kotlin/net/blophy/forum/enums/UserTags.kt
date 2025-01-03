package net.blophy.forum.enums

enum class UserTags(val id: Int) {
    NONE(0),
    DEVELOPER(1),
    CODE_CONTRIBUTOR(2),
    COMMUNITY_ADMIN(3),
    CHART_ASSESSOR(4),
    EXPERIENCED_MAPPER(5),
    LOVED_PLAYER(6),
    OLD_PLAYER(7),
    ;

    companion object {
        private val map = UserTags.entries.toTypedArray().associateBy(UserTags::id)
        fun fromId(typeId: Int) = map[typeId] ?: NONE
    }
}