package com.aminivan.mynotes.viewmodel

import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.response.*
import com.aminivan.mynotes.service.ApiService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Call

class NotesViewModelTest {
    lateinit var service: ApiService
    @Before
    fun setUp(){
        service = mockk()
    }

    @Test
    fun testRetriveNotes(): Unit = runBlocking {
        val responseRetrive = mockk<Call<ResponseFetchAll>>()

        every {
            runBlocking {
                service.getNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc")
            }
        } returns responseRetrive
        val result = service.getNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc")

        verify {
            runBlocking {
                service.getNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc")
            }
        }
        assertEquals(result,responseRetrive)
    }

    @Test
    fun testDeleteNotes(): Unit {
        val responseDelete = mockk<Call<ResponseFetchAll>>()
        every {
            service.deleteNotes("","")
        } returns responseDelete

        val result = service.deleteNotes("","")

        verify {
            service.deleteNotes("","")
        }
        assertEquals(result,responseDelete)
    }

    @Test
    fun testUpdateUser(): Unit = runBlocking {
        val responseUpdateUser = mockk<Call<UserResponseItem>>()
        val dummyuser = UserResponseItem("DUMMY",0,"dummy@gmail.com","dummy","profile","F")
        every {
            runBlocking {
                service.updateUser("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc",dummyuser)
            }
        } returns responseUpdateUser
        val result = service.updateUser("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc",dummyuser)

        verify {
            runBlocking {
                service.updateUser("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc",dummyuser)
            }
        }
        assertEquals(result,responseUpdateUser)
    }

    @Test
    fun testUpdateNotes(): Unit = runBlocking {
        val responseUpdateNotes = mockk<Call<UpdateNotesResponse>>()
        val dummyNotes = NoteResponseItem(0,"title","desc","04/11/2022",3,"image")
        every {
            runBlocking {
                service.updateNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc","2",dummyNotes)
            }
        } returns responseUpdateNotes
        val result = service.updateNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc","2",dummyNotes)

        verify {
            runBlocking {
                service.updateNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc","2",dummyNotes)
            }
        }
        assertEquals(result,responseUpdateNotes)
    }

    @Test
    fun testPostNotes(): Unit = runBlocking {
        val responsePostNotes = mockk<Call<PostNotesResponse>>()
        val dummyNotes = NoteResponseItem(0,"title","desc","04/11/2022",3,"image")
        every {
            runBlocking {
                service.createNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc",dummyNotes)
            }
        } returns responsePostNotes
        val result = service.createNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc",dummyNotes)

        verify {
            runBlocking {
                service.createNotes("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1IiwiZXhwIjoxNjk5MDg5NTI4LCJpYXQiOjE2Njc1NTM1MjgsImlzcyI6ImFtaW5pdmFuIn0.A1srn810rwLwLeoaUl1zJaoTcy5noFB8Gs10hY_cGDc",dummyNotes)
            }
        }
        assertEquals(result,responsePostNotes)
    }

}