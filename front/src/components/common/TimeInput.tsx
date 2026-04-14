import React, { useState, useEffect } from "react";

interface TimeInputProps {
  label: string;
  value?: number;
  onChange: (val?: number) => void;
}

export default function TimeInput({ label, value, onChange }: TimeInputProps) {
  const [hStr, setHStr] = useState("");
  const [mStr, setMStr] = useState("");
  const [sStr, setSStr] = useState("");

  useEffect(() => {
    if (value === undefined) {
      setHStr("");
      setMStr("");
      setSStr("");
    } else {
      const computedTotal =
        parseInt(hStr || "0", 10) * 3600 +
        parseInt(mStr || "0", 10) * 60 +
        parseInt(sStr || "0", 10);
        
      if (computedTotal !== value) {
        const h = Math.floor(value / 3600);
        const m = Math.floor((value % 3600) / 60);
        const s = value % 60;
        setHStr(h > 0 ? h.toString() : "");
        setMStr(m > 0 || h > 0 ? m.toString() : "");
        setSStr(s.toString());
      }
    }
  }, [value]);

  const handleChange = (type: "h" | "m" | "s", valStr: string) => {
    let cleanVal = valStr;

    if (cleanVal.length > 1 && cleanVal.startsWith("0")) {
      cleanVal = cleanVal.replace(/^0+/, "");
      if (cleanVal === "") cleanVal = "0";
    }

    if (type === "m" || type === "s") {
      let num = parseInt(cleanVal, 10);
      if (num > 59) {
        if (cleanVal.endsWith("0")) {
          cleanVal = cleanVal.slice(0, -1);
          num = parseInt(cleanVal, 10);
        }
        if (num > 59) {
          cleanVal = "59";
        }
      }
    }

    const nextH = type === "h" ? cleanVal : hStr;
    const nextM = type === "m" ? cleanVal : mStr;
    const nextS = type === "s" ? cleanVal : sStr;

    setHStr(nextH);
    setMStr(nextM);
    setSStr(nextS);

    if (nextH === "" && nextM === "" && nextS === "") {
      onChange(undefined);
    } else {
      onChange(
        parseInt(nextH || "0", 10) * 3600 +
        parseInt(nextM || "0", 10) * 60 +
        parseInt(nextS || "0", 10)
      );
    }
  };

  return (
    <div>
      <label className="block text-xs font-bold mb-1">{label}</label>
      <div className="flex items-center gap-1">
        <input
          type="number"
          min="0"
          placeholder="시"
          value={hStr}
          onChange={(e) => handleChange("h", e.target.value)}
          className="w-12 px-1 py-2 bg-white border-[2px] border-gray-300 rounded-lg text-sm outline-none text-center"
        />
        <span className="text-gray-500 font-bold">:</span>
        <input
          type="number"
          min="0"
          max="59"
          placeholder="분"
          value={mStr}
          onChange={(e) => handleChange("m", e.target.value)}
          className="w-12 px-1 py-2 bg-white border-[2px] border-gray-300 rounded-lg text-sm outline-none text-center"
        />
        <span className="text-gray-500 font-bold">:</span>
        <input
          type="number"
          min="0"
          max="59"
          placeholder="초"
          value={sStr}
          onChange={(e) => handleChange("s", e.target.value)}
          className="w-12 px-1 py-2 bg-white border-[2px] border-gray-300 rounded-lg text-sm outline-none text-center"
        />
      </div>
    </div>
  );
}
