using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryApp.Converters
{
    public class NonClosingStream : Stream
    {
        private readonly Stream _inner;

        public NonClosingStream(Stream inner) => _inner = inner;

        public override bool CanRead => _inner.CanRead;
        public override bool CanSeek => _inner.CanSeek;
        public override bool CanWrite => _inner.CanWrite;
        public override long Length => _inner.Length;
        public override long Position
        {
            get => _inner.Position;
            set => _inner.Position = value;
        }

        public override void Flush() => _inner.Flush();
        public override int Read(byte[] buffer, int o, int c) => _inner.Read(buffer, o, c);
        public override long Seek(long o, SeekOrigin w) => _inner.Seek(o, w);
        public override void SetLength(long v) => _inner.SetLength(v);
        public override void Write(byte[] buffer, int o, int c) => _inner.Write(buffer, o, c);

        protected override void Dispose(bool disposing)
        {
        }

        public override void Close()
        {
        }

        public void ReallyDispose()
        {
            _inner.Dispose();
        }
    }


}
