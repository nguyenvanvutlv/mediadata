package com.nvv.mediadata.data.connecttv

import java.net.InetAddress


data class TvDevice(
	val name: String,
	val host: InetAddress,
	val port: Int,
)

