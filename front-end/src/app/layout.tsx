import type { Metadata } from "next"
import { Providers } from "./providers"
import "./globals.css"

export const metadata: Metadata = {
    title: "",
    description: "",
}

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode}>) {
    return (
        <html lang="pt-br" className={`h-full antialiased`}>
            <body className="min-h-full flex flex-col">{children}</body>
        </html>
    )
}
