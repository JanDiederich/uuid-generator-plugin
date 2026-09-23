import java.util.UUID

fun uuidPreview() {
    val random = UUID.fromString(/*<# UUID v4 #>*/"550e8400-e29b-41d4-a716-446655440000")
    /*<# UUID v7 2026-04-10 20:00:00.000 +02:00 [Europe/Berlin] #>*/
    val ordered = UUID.fromString("019d788d-0100-7000-a44d-7a782d4e75e2")
}