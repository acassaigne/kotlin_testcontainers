/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package kotlin_testcontainers

import Ticket
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class Repository(jdbcUrl: String, username: String, password: String) {
    private val storageConnection = DriverManager.getConnection(jdbcUrl, username, password)
    fun createTableTicket() {
        val createTableStatement = storageConnection.prepareStatement(
            """ 
                create table if not exists ticket(
                    id decimal(8) primary key,
                    park_time_minutes decimal(4,0)
                )
            """.trimIndent()
        )
        createTableStatement.execute()
    }

    fun saveTicket(ticket: Ticket) {
        val insertStatement = storageConnection.prepareStatement(
            "insert into ticket(id, park_time_minutes) values (?, ?)"
        )
        insertStatement.setInt(1, ticket.id)
        insertStatement.setInt(2, ticket.elapseMinutes)
        insertStatement.execute()
    }

    fun cardinalityTickets(): Int {
        val selectStatement = storageConnection.prepareStatement(
            "select count(*) as cardinalityTickets from ticket"
        )
        val result = selectStatement.executeQuery()
        result.next()
        return result.getInt("cardinalityTickets")
    }
}

class AppTest {
    @Test
    fun appHasAGreeting() {
        val classUnderTest = App()
        assertNotNull(classUnderTest.greeting, "app should have a greeting")
    }

    @Test
    fun testContainerPostgres() {
        // Arrange
        val postgres = PostgreSQLContainer("postgres:16")
        postgres.start()
        val repo = Repository(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        repo.createTableTicket()
        repo.saveTicket(Ticket(id = 1, elapseMinutes = 30))
        repo.saveTicket(Ticket(id = 2, elapseMinutes = 18))

        // Act
        val countTickets = repo.cardinalityTickets()

        postgres.stop()

        // Assert
        assertEquals(2, countTickets)
    }

        @Test fun testContainerMySql() {
        val mysql = MySQLContainer("mysql:8")
        mysql.start()
        mysql.stop()
        assertEquals("yes", "yes")
    }

}
