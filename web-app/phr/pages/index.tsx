
import Link from 'next/link'
import { Inter } from 'next/font/google'

const inter = Inter({ subsets: ['latin'] })

export default function Home() {
  return <>
    <Link href="/bluetooth">Hello</Link>
  </>
}
