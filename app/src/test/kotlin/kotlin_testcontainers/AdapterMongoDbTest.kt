package kotlin_testcontainers

import com.mongodb.kotlin.client.MongoClient
import org.bson.codecs.pojo.annotations.BsonId
import org.testcontainers.containers.MongoDBContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


data class DTOMongoTicket(
    @BsonId
    val id: Int,
    val parkTimeMinutes: Int
)

class RepositoryMongoDb(connexionUrl: String) {
    private val mongoClient = MongoClient.create(connexionUrl)
    private val db = mongoClient.getDatabase("tickets")
    private val ticketCollection = db.getCollection<DTOMongoTicket>("ledgerTicket")

    fun createTicket(ticket: DTOMongoTicket) {
        ticketCollection.insertOne(ticket)
    }

    fun countTickets(): Long {
        return ticketCollection.countDocuments()
    }

    fun getTickets(): List<DTOMongoTicket> {
        val l = mutableListOf<DTOMongoTicket>()
        return ticketCollection.find().toCollection(l)
    }

}

class AdapterMongoDbTest {
    @Test
    fun appHasAGreeting() {
        val classUnderTest = App()
        assertNotNull(classUnderTest.greeting, "app should have a greeting")
    }

    @Test
    fun testEmptyDatabaseHasZeroTicket() {
        // Arrange
        val mongoDB = MongoDBContainer("mongo:8")
        mongoDB.start()
        val connectionUri = mongoDB.connectionString;
        val emptyMongoRepo = RepositoryMongoDb(connectionUri)

        // Act
        val countTicket = emptyMongoRepo.countTickets()

        // Assert
        assertEquals(0, countTicket)

        mongoDB.stop()
    }

    @Test
    fun testSaveOneTicket() {
        // Arrange
        val mongoDB = MongoDBContainer("mongo:8")
        mongoDB.start()
        val connectionUri = mongoDB.connectionString;
        val mongoRepo = RepositoryMongoDb(connectionUri)

        mongoRepo.createTicket(DTOMongoTicket(1, 78))
        val countTicket = mongoRepo.countTickets()

        assertEquals(1, countTicket)

        mongoDB.stop()
    }

    @Test
    fun testList() {
        // Arrange
        val mongoDB = MongoDBContainer("mongo:8")
        mongoDB.start()
        val connectionUri = mongoDB.connectionString;
        val mongoRepo = RepositoryMongoDb(connectionUri)

        mongoRepo.createTicket(DTOMongoTicket(11, 78))
        mongoRepo.createTicket(DTOMongoTicket(12, 46))
        val tickets = mongoRepo.getTickets()

        assertEquals(
            listOf(
                DTOMongoTicket(id = 11, parkTimeMinutes = 78),
                DTOMongoTicket(id = 12, parkTimeMinutes = 46)
            ), tickets
        )

        mongoDB.stop()
    }

}