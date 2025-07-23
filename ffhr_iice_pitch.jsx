import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Presentation, Users, Map, LineChart, HelpingHand, Goal, Lightbulb, BarChart3, HandCoins, Factory } from "lucide-react";
import { motion } from "framer-motion";

export default function FFHRPresentation() {
  return (
    <div className="grid gap-6 p-6 max-w-4xl mx-auto">
      <motion.h1 className="text-3xl font-bold text-center" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
        FFHR – Farmer’s Farm Health Report
      </motion.h1>

      <Slide title="About the Concept" Icon={Presentation}>
        <p>FFHR is a student-led innovation aiming to empower farmers through a low-cost digital platform that offers personalized farm health reports using pH testing, satellite data, weather APIs, and AI.</p>
      </Slide>

      <Slide title="About the Team" Icon={Users}>
        <p>Team of 3 students led by Vaibhav Bhadoriya (B.Tech CSE – Cybersecurity, 1st Year). Passionate about tech, nature, and innovation. Currently seeking a faculty mentor for domain guidance.</p>
      </Slide>

      <Slide title="The Problem" Icon={Map}>
        <ul className="list-disc pl-6">
          <li>Overuse of fertilizers and pesticides</li>
          <li>Lack of affordable NPK/soil testing for small farmers</li>
          <li>Limited access to personalized, real-time crop advice</li>
        </ul>
      </Slide>

      <Slide title="Our Solution" Icon={LineChart}>
        <ul className="list-disc pl-6">
          <li>pH test + cow dung patch method</li>
          <li>Geo-coordinates + NASA POWER + ISRO Bhuvan</li>
          <li>AI-based NPK estimation and disease risk prediction</li>
          <li>Farmer-friendly web/mobile interface</li>
        </ul>
      </Slide>

      <Slide title="What Makes FFHR Unique?" Icon={Lightbulb}>
        <ul className="list-disc pl-6">
          <li>Low-cost, accessible patch testing method</li>
          <li>Multi-API fusion (SoilGrids, Bhuvan, POWER)</li>
          <li>No need for lab NPK testing</li>
          <li>Designed for marginal and small-scale farmers</li>
        </ul>
      </Slide>

      <Slide title="Roadmap & Future Vision" Icon={Goal}>
        <ul className="list-disc pl-6">
          <li>Q3 2025: Prototype + mentorship</li>
          <li>Q4 2025: Field testing with farmers</li>
          <li>2026: Public launch of MVP</li>
          <li>Later: Integration with government schemes/CSR</li>
        </ul>
      </Slide>

      <Slide title="Revenue Model (Optional)" Icon={HandCoins}>
        <p>Impact-first model. Free core services for farmers. Optional premium features with nominal subscription. CSR and institutional partnerships envisioned for sustainability.</p>
      </Slide>

      <Slide title="Market Opportunity" Icon={BarChart3}>
        <ul className="list-disc pl-6">
          <li>85% of Indian farmers are small/marginal</li>
          <li>₹30,000+ Cr spent on fertilizers annually</li>
          <li>Rapid growth of rural agri-digital tech adoption</li>
        </ul>
      </Slide>

      <Slide title="Support Needed from IISER/IICE" Icon={HelpingHand}>
        <ul className="list-disc pl-6">
          <li>Mentorship in soil science, AI, GIS</li>
          <li>Access to labs and testing facilities (if possible)</li>
          <li>Networking with agri-tech experts</li>
          <li>Support for prototype development and validation</li>
        </ul>
      </Slide>

      <motion.div className="text-center mt-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
        <Button className="text-lg px-6 py-3">Thank you! Looking forward to your guidance 🙏</Button>
      </motion.div>
    </div>
  );
}

function Slide({ title, children, Icon }) {
  return (
    <Card className="shadow-md rounded-2xl">
      <CardContent className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Icon className="w-6 h-6 text-green-600" />
          <h2 className="text-xl font-semibold">{title}</h2>
        </div>
        <div className="text-gray-700 text-base">{children}</div>
      </CardContent>
    </Card>
  );
}
