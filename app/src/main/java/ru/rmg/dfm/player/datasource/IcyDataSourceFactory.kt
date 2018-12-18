package ru.rmg.dfm.player.datasource

import com.google.android.exoplayer2.upstream.DataSource

/**
 * Created by Arthur Korchagin on 11.06.17
 */
class IcyDataSourceFactory(val mListener: IcyDataSource.Listener?) : DataSource.Factory {

    override fun createDataSource() = IcyDataSource(mListener)

}