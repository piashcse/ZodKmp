package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertTrue

class ZodStringFormatTest {

    @Test
    fun `uuid should accept valid uuid`() {
        assertTrue(Zod.string().uuid().safeParse("123e4567-e89b-12d3-a456-426614174000") is ZodResult.Success)
    }

    @Test
    fun `uuid should reject invalid uuid`() {
        assertTrue(Zod.string().uuid().safeParse("not-a-uuid") is ZodResult.Failure)
    }

    @Test
    fun `ipv4 should accept valid ipv4`() {
        assertTrue(Zod.string().ipv4().safeParse("192.168.0.1") is ZodResult.Success)
    }

    @Test
    fun `ipv4 should reject invalid ipv4`() {
        assertTrue(Zod.string().ipv4().safeParse("999.1.1.1") is ZodResult.Failure)
        assertTrue(Zod.string().ipv4().safeParse("192.168.1") is ZodResult.Failure)
    }

    @Test
    fun `ipv6 should accept valid ipv6`() {
        assertTrue(Zod.string().ipv6().safeParse("2001:db8:85a3:0:0:8a2e:370:7334") is ZodResult.Success)
        assertTrue(Zod.string().ipv6().safeParse("::1") is ZodResult.Success)
    }

    @Test
    fun `ipv6 should reject invalid ipv6`() {
        assertTrue(Zod.string().ipv6().safeParse("2001:::") is ZodResult.Failure)
    }

    @Test
    fun `ip should accept both ipv4 and ipv6`() {
        assertTrue(Zod.string().ip().safeParse("10.0.0.1") is ZodResult.Success)
        assertTrue(Zod.string().ip().safeParse("fe80::1") is ZodResult.Success)
        assertTrue(Zod.string().ip().safeParse("not-an-ip") is ZodResult.Failure)
    }

    @Test
    fun `emoji should accept emoji strings`() {
        assertTrue(Zod.string().emoji().safeParse("😀") is ZodResult.Success)
        assertTrue(Zod.string().emoji().safeParse("👨‍👩‍👧‍👦") is ZodResult.Success)
        assertTrue(Zod.string().emoji().safeParse("❤️") is ZodResult.Success)
    }

    @Test
    fun `emoji should reject non emoji strings`() {
        assertTrue(Zod.string().emoji().safeParse("hello") is ZodResult.Failure)
        assertTrue(Zod.string().emoji().safeParse("") is ZodResult.Failure)
    }

    @Test
    fun `cuid should accept valid cuid`() {
        assertTrue(Zod.string().cuid().safeParse("cjld2cjxh0000qzrmn831i7rn") is ZodResult.Success)
        assertTrue(Zod.string().cuid().safeParse("cjld2cjxh") is ZodResult.Success)
    }

    @Test
    fun `cuid should reject invalid cuid`() {
        assertTrue(Zod.string().cuid().safeParse("not-a-cuid") is ZodResult.Failure)
    }

    @Test
    fun `cuid2 should accept valid cuid2`() {
        assertTrue(Zod.string().cuid2().safeParse("c2a2y2z2w2x2v2u2t2s2r2q") is ZodResult.Success)
    }

    @Test
    fun `cuid2 should reject invalid cuid2`() {
        assertTrue(Zod.string().cuid2().safeParse("NOT-A-CUID2") is ZodResult.Failure)
    }

    @Test
    fun `ulid should accept valid ulid`() {
        assertTrue(Zod.string().ulid().safeParse("01ARZ3NDEKTSV4RRFFQ69G5FAV") is ZodResult.Success)
    }

    @Test
    fun `ulid should reject invalid ulid`() {
        assertTrue(Zod.string().ulid().safeParse("01ARZ3NDEKTSV4RRFFQ69G5FA") is ZodResult.Failure)
    }

    @Test
    fun `nanoid should accept valid nanoid`() {
        assertTrue(Zod.string().nanoid().safeParse("V1StGXR8_Z5jdHi6B-myT") is ZodResult.Success)
    }

    @Test
    fun `nanoid should reject invalid nanoid`() {
        assertTrue(Zod.string().nanoid().safeParse("not valid!") is ZodResult.Failure)
    }

    @Test
    fun `base64 should accept valid base64`() {
        assertTrue(Zod.string().base64().safeParse("aGVsbG8=") is ZodResult.Success)
        assertTrue(Zod.string().base64().safeParse("YWJj") is ZodResult.Success)
    }

    @Test
    fun `base64 should reject invalid base64`() {
        assertTrue(Zod.string().base64().safeParse("not base64!") is ZodResult.Failure)
    }

    @Test
    fun `base64url should accept valid base64url`() {
        assertTrue(Zod.string().base64url().safeParse("aGVsbG8_") is ZodResult.Success)
        assertTrue(Zod.string().base64url().safeParse("aGVsbG8-") is ZodResult.Success)
    }

    @Test
    fun `base64url should reject invalid base64url`() {
        assertTrue(Zod.string().base64url().safeParse("aGVsbG8+") is ZodResult.Failure)
    }

    @Test
    fun `datetime should accept iso8601 datetimes`() {
        assertTrue(Zod.string().datetime().safeParse("2023-01-01T12:00:00") is ZodResult.Success)
        assertTrue(Zod.string().datetime().safeParse("2023-01-01T12:00:00Z") is ZodResult.Success)
        assertTrue(Zod.string().datetime().safeParse("2023-01-01") is ZodResult.Success)
    }

    @Test
    fun `datetime should reject invalid datetimes`() {
        assertTrue(Zod.string().datetime().safeParse("not-a-date") is ZodResult.Failure)
    }
}
