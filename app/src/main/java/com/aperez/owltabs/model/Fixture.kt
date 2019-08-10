package com.aperez.owltabs.model

data class Fixture(
    val id: Int,
    val type: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val date: String,
    val competitionStage: CompetitionStage,
    val venue: Venue,
    val state: State? = null,
    val score: Score? = null
)

data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val abbr: String,
    val alias: String
)

data class CompetitionStage(
    val competition: Competition,
    val stage: String? = null,
    val leg: String? = null
)

data class Competition(
    val id: String,
    val name: String
)

data class Venue(
    val id: String,
    val name: String
)

data class Score(
    val home: Int,
    val away: Int,
    val winner: PlayStatus? = null
)

enum class State {
    preMatch, postponed, finished
}

enum class PlayStatus {
    home, away
}