BerkeleyDBJ : Base Persistence BTreeFeatures [Itracing] [Statistics] :: _BerkeleyDBJ ;

Persistence : IO LogSize Checksum :: _Persistence ;

IO : OldIO | NewIO :: _IO ;

NewIO : NIOBase NIOType [DirectNIO] :: _NewIO ;

NIOType : ChunkedNIO | SingleWriteNIO :: _NIOType ;

LogSize : S100MiB | S1MiB :: _LogSize ;

BTreeFeatures : [INCompressor] IEvictor [Verifier] :: _BTree ;

IEvictor : Evictor Critical_Eviction :: _IEvictor ;

Itracing : Tracing TracingLevel :: _Itracing ;

TracingLevel : Severe | Finest :: _TracingLevel ;

%% //Semantic Dependencies
Verifier implies INCompressor;
