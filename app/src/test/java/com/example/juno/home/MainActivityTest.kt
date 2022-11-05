package com.example.juno.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class MainActivityTest{
    private lateinit var mainViewModel: MainViewModel

    @Rule @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup(){
        val savedStateHandle = SavedStateHandle().apply {
            set("KEY", "something")
        }
      mainViewModel = MainViewModel(savedStateHandle)
    }
    @Test
    fun `mainViewModel_TestValidateBTC`(){
        val btcList = arrayListOf(Pair("16ftSEQ4ctQFDtVZiUBusQUjRrGhM3JYwe",true)
            ,Pair("3D2oetdNuZUqQHPJmcMDDHYoqkyNVsFk9r",true)
            , Pair("6D2oetdNuZUqQHPJmcMDDHYoqkyNVsFk9r",false)
        )
        btcList.forEach{
            mainViewModel.setBTC(it.first)
            mainViewModel.validateBTC()
            assertEquals(it.second , mainViewModel.valid.value)
        }
    }

    @Test
    fun `mainViewModel_TestValidateETH`(){
        val ethList = arrayListOf(Pair("0x6D31165d5D932D571F3B44695653b46dCC327E84",true)
        ,Pair("16ftSEQ4ctQFDtVZiUBusQUjRrGhM3JYwe",false)
        )
        ethList.forEach{
            mainViewModel.setETH(it.first)
            mainViewModel.validateETH()
            assertEquals(it.second , mainViewModel.valid.value)
        }
    }
}