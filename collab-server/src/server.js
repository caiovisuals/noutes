const { Server } = require('@hocuspocus/server')
const { Database } = require('@hocuspocus/extension-database')
const { Pool } = require('pg')

const pool = new Pool({ connectionString: process.env.DATABASE_URL })

async function ensureTable() {
    await pool.query(`
        CREATE TABLE IF NOT EXISTS ydoc_store (
        page_id TEXT PRIMARY KEY,
        data    BYTEA NOT NULL,
        updated_at TIMESTAMPTZ DEFAULT NOW()
        )
    `)
}

const server = Server.configure({
    port: process.env.PORT || 3001,

    async onAuthenticate({ token, documentName }) {
        const res = await fetch(`${process.env.API_URL}/auth/validate`, {
            headers: { Authorization: `Bearer ${token}` },
        })
        if (!res.ok) throw new Error('Unauthorized')

        const user = await res.json()

        const pageRes = await fetch(`${process.env.API_URL}/pages/${documentName}/access`, {
            headers: { Authorization: `Bearer ${token}` },
        })
        if (!pageRes.ok) throw new Error('Forbidden')

        return { user }
    },

    extensions: [
        new Database({
            fetch: async ({ documentName }) => {
                const result = await pool.query(
                'SELECT data FROM ydoc_store WHERE page_id = $1',
                [documentName]
                )
                return result.rows[0]?.data ?? null
            },
            store: async ({ documentName, state }) => {
                await pool.query(
                `INSERT INTO ydoc_store (page_id, data, updated_at)
                VALUES ($1, $2, NOW())
                ON CONFLICT (page_id) DO UPDATE SET data = $2, updated_at = NOW()`,
                [documentName, state]
                )
            },
        }),
    ],
})

ensureTable().then(() => {
    server.listen()
    console.log(`Collab server running on port ${process.env.PORT || 3001}`)
})